package dev.camunda.bpmn.editor.fileEditor;

import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createCloseScriptFileJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createCopyJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createExportAsSvgJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createInitBpmnJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createOpenScriptFileJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createPasteJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createRedoJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createSaveBpmnJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createSaveSvgJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createSetBaseUrlJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createSetBpmnLintrcJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createSetClipboardJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createSetFocusScriptFileJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createShowErrorNotifictionJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createShowNotifictionJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createUndoJSQuery;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import dev.camunda.bpmn.editor.jcef.Browser;
import dev.camunda.bpmn.editor.jcef.BrowserService;
import dev.camunda.bpmn.editor.jcef.jsquery.JSQuery;
import dev.camunda.bpmn.editor.project.ClipboardManager;
import dev.camunda.bpmn.editor.project.ImageService;
import dev.camunda.bpmn.editor.project.ProjectService;
import dev.camunda.bpmn.editor.server.Server;
import dev.camunda.bpmn.editor.server.handler.ClipboardServerHandler;
import dev.camunda.bpmn.editor.server.handler.LintServerHandler;
import dev.camunda.bpmn.editor.server.handler.UIServerHandler;
import dev.camunda.bpmn.editor.settings.BpmnEditorSettings;
import dev.camunda.bpmn.editor.ui.component.panel.EnginePanel;
import dev.camunda.bpmn.editor.ui.component.popupmenu.EditorPopupMenu;
import dev.camunda.bpmn.editor.ui.component.toolbar.EditorToolBarBuilder;
import dev.camunda.bpmn.editor.vfs.BpmnFile;
import dev.camunda.bpmn.editor.vfs.ScriptFileManager;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom file editor for BPMN (Business Process Model and Notation) files.
 * This editor integrates with the BPMN Editor Browser to provide a specialized
 * interface for editing BPMN files within the IntelliJ IDEA environment.
 * It allows users to visually create, edit, and manage BPMN diagrams directly
 * in the IDE, enhancing the workflow for business process modeling.
 *
 * <p>The editor initializes various services and components necessary for BPMN editing:
 * <ul>
 *   <li>HTTP server for handling BPMN editor UI requests</li>
 *   <li>JCEFBrowser for rendering the BPMN editor interface</li>
 *   <li>Clipboard service for copy-paste operations</li>
 *   <li>Script file manager for handling external script files</li>
 *   <li>JavaScript queries for browser-backend communication</li>
 * </ul>
 *
 * <p>The editor supports two modes of operation:
 * <ul>
 *   <li>Direct BPMN editing when the engine is set</li>
 *   <li>Engine selection interface when the engine is not set</li>
 * </ul>
 *
 * <p>This class implements the {@link FileEditor} interface, providing integration
 * with IntelliJ IDEA's file editing framework.
 *
 * @author Oleksandr Havrysh
 */
public class BpmnFileEditor implements FileEditor {

    private static final String BPMN_EDITOR = "BPMN Editor";

    @Getter
    private final JComponent component;

    @Getter
    private final JComponent preferredFocusedComponent;

    private final Server server;
    private final BpmnFile bpmnFile;
    private final BrowserService browserService;
    private final ScriptFileManager scriptFileManager;

    /**
     * Constructs a new BpmnFileEditor instance.
     *
     * <p>This constructor initializes all necessary parts and services for the BPMN editor:
     * <ul>
     *   <li>Project service for project-related operations</li>
     *   <li>HTTP server for handling BPMN editor UI requests</li>
     *   <li>JCEFBrowser wrapper for rendering the BPMN editor interface</li>
     *   <li>Clipboard service for copy-paste operations</li>
     *   <li>Virtual file service for file operations</li>
     *   <li>Script file manager for handling external script files</li>
     *   <li>JavaScript queries for browser-backend communication</li>
     *   <li>JCEFBrowser service for managing the browser component</li>
     * </ul>
     *
     * <p>The constructor also determines whether to display the BPMN editor directly
     * or show an engine selection interface based on the current settings.
     *
     * @param project The IntelliJ IDEA project context
     * @param file    The virtual file representing the BPMN file to be edited
     */
    public BpmnFileEditor(Project project, VirtualFile file) {
        var clipboardService = new ClipboardManager();
        var projectService = new ProjectService(project);
        var bpmnEditorUIHandler = new UIServerHandler();
        var lintPluginHandler = new LintServerHandler(projectService);
        var clipboardHandler = new ClipboardServerHandler(clipboardService);
        this.server = new Server(bpmnEditorUIHandler, lintPluginHandler, clipboardHandler);

        var editorPopupMenu = new EditorPopupMenu();
        var browser = new Browser(editorPopupMenu);
        setEditorPopupMenuActions(editorPopupMenu, browser);

        this.bpmnFile = new BpmnFile(file);
        this.scriptFileManager = new ScriptFileManager(projectService, browser);
        var state = BpmnEditorSettings.getInstance().getState();
        var imageService = new ImageService(projectService);
        var initQueries = createInitJsQueries(browser, projectService, state, clipboardService, imageService);
        this.browserService = new BrowserService(initQueries, browser, server, bpmnFile);

        this.preferredFocusedComponent = state.isEngineSet(bpmnFile.getPath()) ? browserService.loadBpmn() :
                new EnginePanel(result -> state.addFileSettings(bpmnFile.getPath(), result),
                        browserService::loadBpmn);

        this.component = !state.getUseToolBar() ? preferredFocusedComponent :
                new EditorToolBarBuilder(preferredFocusedComponent)
                        .undoAction(createUndoJSQuery(browser))
                        .redoAction(createRedoJSQuery(browser))
                        .separator()
                        .copyAction(createCopyJSQuery(browser))
                        .pasteAction(createPasteJSQuery(browser))
                        .separator()
                        .exportAsSvgAction(createExportAsSvgJSQuery(browser))
                        .build();
    }

    /**
     * Sets up the actions for the editor's popup menu.
     * This method configures various menu items with their corresponding JavaScript queries.
     *
     * @param editorPopupMenu The popup menu to be configured
     * @param browser         The browser instance used to execute JavaScript queries
     */
    private void setEditorPopupMenuActions(EditorPopupMenu editorPopupMenu, Browser browser) {
        editorPopupMenu.undoMenuItemAction(createUndoJSQuery(browser))
                .redoMenuItemAction(createRedoJSQuery(browser))
                .copyMenuItemAction(createCopyJSQuery(browser))
                .pasteMenuItemAction(createPasteJSQuery(browser))
                .exportAsSvgMenuItemAction(createExportAsSvgJSQuery(browser));
    }

    /**
     * Creates a list of initial JavaScript queries to be executed when the editor is loaded.
     * These queries set up various aspects of the BPMN editor, such as linting, error notifications,
     * and file management.
     *
     * @param browser          The browser instance used to execute JavaScript queries
     * @param projectService   The project service for accessing project-related information
     * @param state            The current state of the BPMN editor settings
     * @param clipboardService The clipboard manager for handling copy-paste operations
     * @param imageService     The image service for handling image-related operations
     * @return A list of JSQuery objects representing the initial queries to be executed
     */
    private List<JSQuery> createInitJsQueries(Browser browser,
                                              ProjectService projectService,
                                              BpmnEditorSettings.State state,
                                              ClipboardManager clipboardService,
                                              ImageService imageService) {
        var initQueries = new ArrayList<JSQuery>(9);
        if (state.getUseBpmnLinter()) {
            initQueries.add(createSetBpmnLintrcJSQuery(browser, projectService));
        }

        initQueries.add(createShowErrorNotifictionJSQuery(browser, projectService));
        initQueries.add(createShowNotifictionJSQuery(browser, projectService));
        initQueries.add(createSetBaseUrlJSQuery(browser, server));
        initQueries.add(createInitBpmnJSQuery(browser, bpmnFile));
        initQueries.add(createCloseScriptFileJSQuery(browser, scriptFileManager));
        initQueries.add(createOpenScriptFileJSQuery(browser, scriptFileManager));
        initQueries.add(createSaveBpmnJSQuery(browser, bpmnFile));
        initQueries.add(createSetClipboardJSQuery(browser, clipboardService));
        initQueries.add(createSetFocusScriptFileJSQuery(browser, scriptFileManager));
        initQueries.add(createSaveSvgJSQuery(browser, imageService));

        return initQueries;
    }

    /**
     * Returns the name of the editor, which is used for display.
     *
     * @return The name of the BPMN editor.
     */
    @Override
    public @NotNull String getName() {
        return BPMN_EDITOR;
    }

    /**
     * Sets the state of the editor. This method is called when the editor's state
     * needs to be updated, for example, when restoring the editor's state after
     * the IDE restarts.
     *
     * @param state The new state of the editor.
     */
    @Override
    public void setState(@NotNull FileEditorState state) {
        // Implementation not provided in the current version
    }

    /**
     * Checks if the editor content has been modified since the last save.
     *
     * @return true if the content has been modified, false otherwise.
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Checks if the editor is in a valid state and can be used.
     *
     * @return true if the editor is valid and can be used, false otherwise.
     */
    @Override
    public boolean isValid() {
        return bpmnFile.isValid();
    }

    /**
     * Returns the virtual file being edited by this editor.
     *
     * @return The VirtualFile object representing the BPMN file being edited.
     */
    @Override
    public VirtualFile getFile() {
        return bpmnFile.getFile();
    }

    /**
     * Adds a property change listener to the editor. This can be used to listen
     * for changes in the editor's properties.
     *
     * @param listener The PropertyChangeListener to be added.
     */
    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // Implementation not provided in the current version
    }

    /**
     * Removes a previously added property change listener from the editor.
     *
     * @param listener The PropertyChangeListener to be removed.
     */
    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // Implementation not provided in the current version
    }

    /**
     * Disposes of the editor and its resources. This method is called when
     * the editor is no longer needed and should release any resources it holds.
     */
    @Override
    public void dispose() {
        scriptFileManager.dispose();
        browserService.dispose();
        server.stop();
    }

    /**
     * Retrieves user-specific data associated with this editor instance.
     *
     * @param key The key to retrieve the user data.
     * @param <T> The type of the user data.
     * @return The user data associated with the key, or null if no data is associated.
     */
    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    /**
     * Associates user-specific data with this editor instance.
     *
     * @param key   The key to associate the user data.
     * @param value The user data to be associated, or null to remove the association.
     * @param <T>   The type of the user data.
     */
    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        // Implementation not provided in the current version
    }
}