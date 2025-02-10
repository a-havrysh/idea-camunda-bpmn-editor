package dev.camunda.bpmn.editor.ui.component.popupmenu;

import static com.intellij.icons.ExpUiIcons.General.Copy;
import static com.intellij.icons.ExpUiIcons.General.Export;
import static com.intellij.icons.ExpUiIcons.General.Paste;
import static com.intellij.icons.ExpUiIcons.General.Redo;
import static com.intellij.icons.ExpUiIcons.General.Undo;

import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import dev.camunda.bpmn.editor.jcef.jsquery.JSQuery;

/**
 * A custom popup menu for the BPMN editor.
 * This class extends JBPopupMenu to provide a context menu with common editing operations
 * such as undo, redo, copy, paste, and export options.
 * Each menu item can be associated with a JSQuery to perform the corresponding action.
 */
public class EditorPopupMenu extends JBPopupMenu {

    private final JBMenuItem undoMenuItem;
    private final JBMenuItem redoMenuItem;
    private final JBMenuItem copyMenuItem;
    private final JBMenuItem pasteMenuItem;
    private final JBMenuItem exportAsSvgMenuItem;

    /**
     * Constructs a new EditorPopupMenu with predefined menu items.
     * The menu includes options for undo, redo, copy, paste, and export operations.
     */
    public EditorPopupMenu() {
        this.undoMenuItem = new JBMenuItem("Undo", Undo);
        this.redoMenuItem = new JBMenuItem("Redo", Redo);

        this.copyMenuItem = new JBMenuItem("Copy", Copy);
        this.pasteMenuItem = new JBMenuItem("Paste", Paste);

        this.exportAsSvgMenuItem = new JBMenuItem("Export as SVG", Export);

        add(undoMenuItem);
        add(redoMenuItem);
        addSeparator();
        add(copyMenuItem);
        add(pasteMenuItem);
        addSeparator();
        add(exportAsSvgMenuItem);

        pack();
    }

    /**
     * Sets the action for the "Export as SVG" menu item.
     *
     * @param jsQuery The JSQuery to be executed when the menu item is selected
     * @return This EditorPopupMenu instance for method chaining
     */
    public EditorPopupMenu exportAsSvgMenuItemAction(JSQuery jsQuery) {
        return addActionListener(exportAsSvgMenuItem, jsQuery);
    }

    /**
     * Sets the action for the "Copy" menu item.
     *
     * @param jsQuery The JSQuery to be executed when the menu item is selected
     * @return This EditorPopupMenu instance for method chaining
     */
    public EditorPopupMenu copyMenuItemAction(JSQuery jsQuery) {
        return addActionListener(copyMenuItem, jsQuery);
    }

    /**
     * Sets the action for the "Paste" menu item.
     *
     * @param jsQuery The JSQuery to be executed when the menu item is selected
     * @return This EditorPopupMenu instance for method chaining
     */
    public EditorPopupMenu pasteMenuItemAction(JSQuery jsQuery) {
        return addActionListener(pasteMenuItem, jsQuery);
    }

    /**
     * Sets the action for the "Undo" menu item.
     *
     * @param jsQuery The JSQuery to be executed when the menu item is selected
     * @return This EditorPopupMenu instance for method chaining
     */
    public EditorPopupMenu undoMenuItemAction(JSQuery jsQuery) {
        return addActionListener(undoMenuItem, jsQuery);
    }

    /**
     * Sets the action for the "Redo" menu item.
     *
     * @param jsQuery The JSQuery to be executed when the menu item is selected
     * @return This EditorPopupMenu instance for method chaining
     */
    public EditorPopupMenu redoMenuItemAction(JSQuery jsQuery) {
        return addActionListener(redoMenuItem, jsQuery);
    }

    /**
     * Adds an action listener to the specified menu item that executes the given JSQuery.
     *
     * @param menuItem The menu item to which the action listener will be added
     * @param jsQuery  The JSQuery to be executed when the menu item is selected
     * @return This EditorPopupMenu instance for method chaining
     */
    private EditorPopupMenu addActionListener(JBMenuItem menuItem, JSQuery jsQuery) {
        menuItem.addActionListener(e -> jsQuery.executeQuery());
        return this;
    }
}