package dev.camunda.bpmn.editor.vfs;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createDeleteVirtualFileIdJSQuery;
import static dev.camunda.bpmn.editor.jcef.jsquery.JSQueryFactory.createUpdateScriptJSQuery;
import static dev.camunda.bpmn.editor.util.Base64Utils.decode;
import static java.lang.String.join;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.Alarm;
import dev.camunda.bpmn.editor.jcef.Browser;
import dev.camunda.bpmn.editor.project.ProjectService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.CustomLog;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A class responsible for creating and managing script files in the BPMN Editor.
 * This class allows for the creation of scratch files with different languages based on the file extension.
 * It also provides methods to manage the lifecycle of the script file, such as setting focus, and deleting the file.
 *
 * @author Oleksandr Havrysh
 */
@CustomLog
public class ScriptFile implements FileEditorManagerListener, DocumentListener, Disposable {

    private static final String DOT = ".";
    private static final String JS = "js";
    private static final String JAVASCRIPT = "javascript";

    @Getter
    private final String virtualFileId;

    private final VirtualFile scriptFile;
    private final Alarm scriptChangeAlarm;
    private final AtomicBoolean isFileClosed;
    private final Browser browser;
    private final ProjectService projectService;
    private final Consumer<String> closeFileConsumer;
    private final AtomicBoolean isDocumentListenerRegistered;

    /**
     * Creates a new ScriptFile instance.
     *
     * @param text              The initial content of the script
     * @param projectService    The projectService in which the script file should be created
     * @param browser           The browser wrapper
     * @param closeFileConsumer The consumer to be called when the file is closed
     */
    public ScriptFile(String text,
                      ProjectService projectService,
                      Browser browser,
                      Consumer<String> closeFileConsumer) {
        this.projectService = projectService;
        this.browser = browser;
        this.closeFileConsumer = closeFileConsumer;
        this.virtualFileId = randomUUID().toString();
        this.scriptChangeAlarm = new Alarm(this);
        this.isFileClosed = new AtomicBoolean(false);
        this.isDocumentListenerRegistered = new AtomicBoolean(false);

        text = isBlank(text) ? "" : text;
        var scriptData = text.split("@");
        var extension = formatExtension(scriptData[1]);
        var fileName = join(DOT, virtualFileId, extension);
        var fileType = FileTypeManager.getInstance().getFileTypeByExtension(extension);
        this.scriptFile = new LightVirtualFile(fileName, fileType, decode(scriptData[0]));

        getApplication().getMessageBus().connect().subscribe(FILE_EDITOR_MANAGER, this);

        setFocus();
    }

    /**
     * Formats the file extension to ensure it is valid.
     *
     * @param extension The original file extension
     * @return The formatted file extension
     */
    private String formatExtension(String extension) {
        return JAVASCRIPT.equalsIgnoreCase(extension) ? JS : extension;
    }

    /**
     * Sets focus to the created scratch file.
     */
    public void setFocus() {
        getApplication().invokeLater(() -> projectService.getFileEditorManager().openFile(scriptFile, true));
    }

    /**
     * Closes the scratch file.
     * This method should be called when the file is no longer needed.
     */
    @Override
    public void dispose() {
        getApplication().invokeLater(() -> projectService.getFileEditorManager().closeFile(scriptFile));
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (!scriptFile.equals(file) || isDocumentListenerRegistered.get()) {
            return;
        }

        var document = FileDocumentManager.getInstance().getDocument(scriptFile);
        if (isNull(document)) {
            return;
        }

        try {
            document.addDocumentListener(this);
            isDocumentListenerRegistered.set(true);
        } catch (Exception e) {
            log.error("Failed to add document listener to script file", e);
        }
    }

    /**
     * Handles the file closed event.
     * Deletes the script file, updates the browser, and notifies close file consumers.
     *
     * @param source The FileEditorManager source
     * @param file   The closed VirtualFile
     */
    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (!scriptFile.equals(file) || isFileClosed.get()) {
            return;
        }

        isFileClosed.set(true);
        createDeleteVirtualFileIdJSQuery(browser, virtualFileId).executeQuery();
        closeFileConsumer.accept(virtualFileId);
    }

    /**
     * Handles document change events.
     * Schedules an update of the script content with debouncing to avoid excessive updates.
     *
     * @param event The DocumentEvent containing change information
     */
    @Override
    public void documentChanged(DocumentEvent event) {
        scriptChangeAlarm.cancelAllRequests();
        var text = event.getDocument().getText();
        scriptChangeAlarm.addRequest(() -> createUpdateScriptJSQuery(browser, virtualFileId, text).executeQuery(), 500);
    }
}