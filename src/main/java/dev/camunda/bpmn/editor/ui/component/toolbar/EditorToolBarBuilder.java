package dev.camunda.bpmn.editor.ui.component.toolbar;

import static com.intellij.icons.ExpUiIcons.General.Copy;
import static com.intellij.icons.ExpUiIcons.General.Export;
import static com.intellij.icons.ExpUiIcons.General.Paste;
import static com.intellij.icons.ExpUiIcons.General.Redo;
import static com.intellij.icons.ExpUiIcons.General.Undo;
import static com.intellij.openapi.actionSystem.ActionPlaces.TOOLBAR;
import static javax.swing.BorderFactory.createEmptyBorder;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.ui.roots.ToolbarPanel;
import dev.camunda.bpmn.editor.jcef.jsquery.JSQuery;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A builder class for creating a customized toolbar for the BPMN editor.
 * This class allows for the easy construction of a toolbar with various actions
 * such as undo, redo, copy, paste, and export operations.
 * <p>
 * The builder pattern is used to configure the toolbar, allowing for a fluent
 * and readable way to add actions and separators to the toolbar.
 */
@RequiredArgsConstructor
public class EditorToolBarBuilder {

    private final JComponent component;
    private final List<AnAction> actions = new ArrayList<>(8);

    /**
     * Adds an "Export as SVG" action to the toolbar.
     *
     * @param jsQuery The JSQuery to be executed when the action is triggered
     * @return This builder instance for method chaining
     */
    public EditorToolBarBuilder exportAsSvgAction(JSQuery jsQuery) {
        return buildAction("Export as SVG", Export, jsQuery);
    }

    /**
     * Adds a "Copy" action to the toolbar.
     *
     * @param jsQuery The JSQuery to be executed when the action is triggered
     * @return This builder instance for method chaining
     */
    public EditorToolBarBuilder copyAction(JSQuery jsQuery) {
        return buildAction("Copy selected content", Copy, jsQuery);
    }

    /**
     * Adds a "Paste" action to the toolbar.
     *
     * @param jsQuery The JSQuery to be executed when the action is triggered
     * @return This builder instance for method chaining
     */
    public EditorToolBarBuilder pasteAction(JSQuery jsQuery) {
        return buildAction("Paste content", Paste, jsQuery);
    }

    /**
     * Adds an "Undo" action to the toolbar.
     *
     * @param jsQuery The JSQuery to be executed when the action is triggered
     * @return This builder instance for method chaining
     */
    public EditorToolBarBuilder undoAction(JSQuery jsQuery) {
        return buildAction("Undo last operation", Undo, jsQuery);
    }

    /**
     * Adds a "Redo" action to the toolbar.
     *
     * @param jsQuery The JSQuery to be executed when the action is triggered
     * @return This builder instance for method chaining
     */
    public EditorToolBarBuilder redoAction(JSQuery jsQuery) {
        return buildAction("Redo last operation", Redo, jsQuery);
    }

    /**
     * Adds a separator to the toolbar.
     *
     * @return This builder instance for method chaining
     */
    public EditorToolBarBuilder separator() {
        return addAction(new Separator());
    }

    /**
     * Builds and returns the configured toolbar panel.
     *
     * @return A ToolbarPanel instance with all the configured actions
     */
    public ToolbarPanel build() {
        var actionGroup = new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return actions.toArray(AnAction[]::new);
            }
        };

        var toolbarPanel = new ToolbarPanel(component, actionGroup, TOOLBAR, null);
        toolbarPanel.setBorder(createEmptyBorder());
        return toolbarPanel;
    }

    /**
     * Builds an action with the given description, icon, and JSQuery.
     *
     * @param description The description of the action
     * @param icon        The icon to be displayed for the action
     * @param jsQuery     The JSQuery to be executed when the action is triggered
     * @return This builder instance for method chaining
     */
    private EditorToolBarBuilder buildAction(String description, Icon icon, JSQuery jsQuery) {
        return addAction(new ToolBarAction(description, icon, jsQuery));
    }

    /**
     * Adds an action to the list of actions for the toolbar.
     *
     * @param action The AnAction to be added to the toolbar
     * @return This builder instance for method chaining
     */
    private EditorToolBarBuilder addAction(AnAction action) {
        actions.add(action);
        return this;
    }

    /**
     * A custom action class for toolbar items that executes a JSQuery when triggered.
     */
    private static class ToolBarAction extends AnAction {

        private final JSQuery jsQuery;

        /**
         * Constructs a new ToolBarAction.
         *
         * @param description The description of the action, used for tooltips
         * @param icon        The icon to be displayed for the action in the toolbar
         * @param jsQuery     The JSQuery to be executed when the action is triggered
         */
        private ToolBarAction(String description, Icon icon, JSQuery jsQuery) {
            super(() -> description, icon);
            this.jsQuery = jsQuery;
        }

        /**
         * Performs the action by executing the associated JSQuery.
         *
         * @param e The action event
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            jsQuery.executeQuery();
        }
    }
}