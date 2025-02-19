package dev.camunda.bpmn.editor.ui.component.panel;

import static com.intellij.ui.JBColor.RED;
import static com.intellij.util.ui.JBUI.Borders.emptyBottom;
import static com.intellij.util.ui.JBUI.Borders.emptyLeft;
import static dev.camunda.bpmn.editor.util.ComponentUtils.createNullableComboBox;
import static java.awt.FlowLayout.LEFT;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.VerticalLayout;
import dev.camunda.bpmn.editor.settings.BpmnEditorSettings;
import dev.camunda.bpmn.editor.ui.component.table.BpmnSettingsTable;
import java.awt.FlowLayout;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * A UI component for configuring the BPMN Editor settings within the IntelliJ IDEA environment.
 * This class provides a comprehensive form with combo boxes, checkboxes, and a table
 * to manage both global and file-specific settings of the BPMN Editor.
 *
 * <p>The component includes options for:</p>
 * <ul>
 *   <li>Global color theme selection</li>
 *   <li>Global schema theme selection</li>
 *   <li>Default script type selection</li>
 *   <li>Default engine selection</li>
 *   <li>BPMN Linter usage toggle</li>
 *   <li>Toolbar usage toggle</li>
 *   <li>File-specific settings management through a table</li>
 * </ul>
 *
 * <p>This component is designed to be used within the settings panel of the BPMN Editor
 * and implements the Disposable interface for proper resource management.</p>
 *
 * @author Oleksandr Havrysh
 */
public class SettingsPanel extends JPanel implements Disposable {

    private final JCheckBox useBpmnLinter;
    private final JCheckBox useToolBar;
    private final BpmnSettingsTable bpmnSettingsTable;
    private final JComboBox<BpmnEditorSettings.Engine> engineComboBox;
    private final JComboBox<BpmnEditorSettings.ScriptType> scriptTypeComboBox;
    private final JComboBox<BpmnEditorSettings.ColorTheme> colorThemeComboBox;
    private final JComboBox<BpmnEditorSettings.SchemaTheme> schemeThemeComboBox;

    /**
     * Constructs a new BpmnEditorSettingsComponent.
     * Initializes the combo boxes with the available options and builds the form.
     */
    public SettingsPanel() {
        super(new VerticalLayout(5));

        this.bpmnSettingsTable = new BpmnSettingsTable();
        this.colorThemeComboBox = new ComboBox<>(BpmnEditorSettings.ColorTheme.values());
        this.schemeThemeComboBox = new ComboBox<>(BpmnEditorSettings.SchemaTheme.values());
        this.engineComboBox = createNullableComboBox(BpmnEditorSettings.Engine.values());
        this.scriptTypeComboBox = createNullableComboBox(BpmnEditorSettings.ScriptType.values());
        this.useBpmnLinter = new JCheckBox("Use BPMN Linter");
        this.useToolBar = new JCheckBox("Use toolbar");

        add(addDisclaimerComponents());
        add(new TitledSeparator("Global Settings"));
        add(addGlobalSettingComponents());
        add(new TitledSeparator("Diagram Settings"));
        add(addDiagramSettingsComponents());
    }

    /**
     * Adds disclaimer components to the settings panel.
     *
     * <p>This method adds three JLabel components to the panel, providing instructions
     * to the user about applying updated settings to currently opened BPMN files.
     * The disclaimer informs users that they need to close and reopen their BPMN files
     * to ensure that new configurations are properly loaded and applied to the diagrams.</p>
     *
     * <p>The disclaimer text is split into three lines for better readability in the UI.</p>
     *
     * @return
     */
    private JPanel addDisclaimerComponents() {
        var disclaimerPanel = new JPanel(new VerticalLayout(3));
        disclaimerPanel.setBorder(emptyLeft(20));
        disclaimerPanel.add(new JBLabel("To apply the updated settings to your currently opened BPMN files, please close the files and reopen them. "));
        disclaimerPanel.add(new JBLabel("This ensures that the new configuration is properly loaded and applied to the diagrams,"));
        disclaimerPanel.add(new JBLabel("reflecting the latest changes."));
        return disclaimerPanel;
    }

    /**
     * Adds components related to global settings to the panel.
     *
     * @return
     */
    private JPanel addGlobalSettingComponents() {
        var useBpmnLinterPanel = new JPanel(new VerticalLayout(3));
        useBpmnLinterPanel.setBorder(emptyBottom(10));
        useBpmnLinterPanel.add(useBpmnLinter);
        useBpmnLinterPanel.add(new JBLabel("Add '.bpmnlintrc' file to the project to enable BPMN linter"));
        useBpmnLinterPanel.add(new JBLabel("For use custom plugins, put the source code or add it to 'package.json' to the project and run 'npm install'"));

        var useToolBarPanel = new JPanel(new VerticalLayout(3));
        useToolBarPanel.add(useToolBar);
        useToolBarPanel.add(new JBLabel("Show toolbar for the editor"));

        var globalSettingPanel = new JPanel(new VerticalLayout(3));
        globalSettingPanel.setBorder(emptyLeft(20));
        globalSettingPanel.add(createComboboxPanel("Color theme:", colorThemeComboBox));
        globalSettingPanel.add(createComboboxPanel("Scheme theme:", schemeThemeComboBox));
        globalSettingPanel.add(createComboboxPanel("Default script type:", scriptTypeComboBox));
        globalSettingPanel.add(createComboboxPanel("Default engine:", engineComboBox));
        globalSettingPanel.add(useBpmnLinterPanel);
        globalSettingPanel.add(useToolBarPanel);

        return globalSettingPanel;
    }

    /**
     * Creates a panel containing a label and a combobox.
     *
     * <p>This method generates a JPanel with a FlowLayout aligned to the left.
     * It adds a JBLabel with the specified text and the provided JComboBox to this panel.</p>
     *
     * <p>This is typically used for creating consistent UI components in settings
     * or configuration panels where a labeled dropdown selection is needed.</p>
     *
     * @param labelText The text to be displayed in the label next to the combobox.
     * @param comboBox  The JComboBox to be added to the panel.
     * @return A JPanel containing the label and combobox, laid out horizontally.
     */
    private JPanel createComboboxPanel(String labelText, JComboBox<?> comboBox) {
        var enginePanel = new JPanel(new FlowLayout(LEFT));
        enginePanel.add(new JBLabel(labelText));
        enginePanel.add(comboBox);

        return enginePanel;
    }

    /**
     * Adds components related to diagram settings to the panel.
     *
     * @return
     */
    private JPanel addDiagramSettingsComponents() {
        var diagramSettingsPanel = new JPanel(new VerticalLayout(3));
        diagramSettingsPanel.setBorder(emptyLeft(20));
        diagramSettingsPanel.add(new JBLabel("This is a list of used BPMN files."));
        diagramSettingsPanel.add(new JBLabel("You can individually configure the engine, theme, and auto script format for inline scripts."));
        diagramSettingsPanel.add(new JBLabel("You can also delete the selected row or clear the table."));
        diagramSettingsPanel.add(new JBLabel("If you select an empty value from the dropdown, the default value will be used if it is set."));
        diagramSettingsPanel.add(new JBScrollPane(bpmnSettingsTable));
        diagramSettingsPanel.add(createTableButtonsPanel());

        return diagramSettingsPanel;
    }

    /**
     * Creates the panel with buttons for managing the table.
     *
     * @return The panel with buttons for managing the table
     */
    private @NotNull JPanel createTableButtonsPanel() {
        var deleteRowButton = new JButton("Delete Row");
        deleteRowButton.addActionListener(e -> bpmnSettingsTable.deleteSelectedRow());
        deleteRowButton.setEnabled(false);
        deleteRowButton.setForeground(RED);
        bpmnSettingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteRowButton.setEnabled(bpmnSettingsTable.getSelectedRow() != -1);
            }
        });

        var clearTableButton = new JButton("Clear Table");
        clearTableButton.addActionListener(e -> bpmnSettingsTable.clearAll());
        clearTableButton.setForeground(RED);

        var tableButtonPanel = new JPanel(new FlowLayout());
        tableButtonPanel.add(deleteRowButton);
        tableButtonPanel.add(clearTableButton);

        return tableButtonPanel;
    }

    /**
     * Returns the preferred component to focus when the settings panel is opened.
     *
     * @return The preferred component to focus
     */
    public @NotNull JComponent getPreferredFocusedComponent() {
        return colorThemeComboBox;
    }

    /**
     * Returns the currently selected color theme.
     *
     * @return The currently selected color theme
     */
    public BpmnEditorSettings.ColorTheme getColorThemeValue() {
        return (BpmnEditorSettings.ColorTheme) colorThemeComboBox.getSelectedItem();
    }

    /**
     * Sets the selected color theme in the combo box.
     *
     * @param colorTheme The color theme to be selected
     */
    public void setColorThemeValue(BpmnEditorSettings.ColorTheme colorTheme) {
        colorThemeComboBox.setSelectedItem(colorTheme);
    }

    /**
     * Retrieves the currently selected schema theme.
     *
     * @return The currently selected {@link BpmnEditorSettings.SchemaTheme}
     */
    public BpmnEditorSettings.SchemaTheme getSchemaThemeValue() {
        return (BpmnEditorSettings.SchemaTheme) schemeThemeComboBox.getSelectedItem();
    }

    /**
     * Sets the selected schema theme in the combo box.
     *
     * @param schemaTheme The {@link BpmnEditorSettings.SchemaTheme} to be selected
     */
    public void setSchemaThemeValue(BpmnEditorSettings.SchemaTheme schemaTheme) {
        schemeThemeComboBox.setSelectedItem(schemaTheme);
    }

    /**
     * Returns the currently selected script type.
     *
     * @return The currently selected script type
     */
    public BpmnEditorSettings.ScriptType getScriptTypeValue() {
        return (BpmnEditorSettings.ScriptType) scriptTypeComboBox.getSelectedItem();
    }

    /**
     * Sets the selected script type in the combo box.
     *
     * @param scriptType The script type to be selected
     */
    public void setScriptTypeValue(BpmnEditorSettings.ScriptType scriptType) {
        scriptTypeComboBox.setSelectedItem(scriptType);
    }

    /**
     * Returns the currently selected engine.
     *
     * @return The currently selected engine
     */
    public BpmnEditorSettings.Engine getEngineValue() {
        return (BpmnEditorSettings.Engine) engineComboBox.getSelectedItem();
    }

    /**
     * Sets the selected engine in the combo box.
     *
     * @param engine The engine to be selected
     */
    public void setEngineValue(BpmnEditorSettings.Engine engine) {
        engineComboBox.setSelectedItem(engine);
    }

    /**
     * Sets the file settings in the table.
     *
     * @param fileSettings The file settings to be set
     */
    public void setFileSettings(Map<String, BpmnEditorSettings.FileSettings> fileSettings) {
        bpmnSettingsTable.setFileSettings(fileSettings);
    }

    /**
     * Returns the file settings from the table.
     *
     * @return The file settings from the table
     */
    public Map<String, BpmnEditorSettings.FileSettings> getFileSettings() {
        return bpmnSettingsTable.getFileSettings();
    }

    /**
     * Returns the state of the BPMN Linter usage checkbox.
     *
     * @return {@code true} if the BPMN Linter is set to be used, {@code false} otherwise
     */
    public Boolean getUseBpmnLinter() {
        return useBpmnLinter.isSelected();
    }

    /**
     * Sets the state of the BPMN Linter usage checkbox.
     *
     * @param useBpmnLinter {@code true} to enable the BPMN Linter, {@code false} to disable it
     */
    public void setUseBpmnLinter(Boolean useBpmnLinter) {
        this.useBpmnLinter.setSelected(useBpmnLinter);
    }

    /**
     * Returns the state of the toolbar usage checkbox.
     *
     * @return {@code true} if the toolbar is set to be used, {@code false} otherwise
     */
    public Boolean getUseToolBar() {
        return useToolBar.isSelected();
    }

    /**
     * Sets the state of the toolbar usage checkbox.
     *
     * @param useToolBar {@code true} to enable the toolbar, {@code false} to disable it
     */
    public void setUseToolBar(Boolean useToolBar) {
        this.useToolBar.setSelected(useToolBar);
    }

    /**
     * Disposes of the resources used by this component.
     * This method is called when the component is no longer needed and should release any resources it holds.
     *
     * <p>It removes all components from this panel and clears all items from the combo boxes.</p>
     */
    @Override
    public void dispose() {
        removeAll();
        scriptTypeComboBox.removeAll();
        colorThemeComboBox.removeAll();
        engineComboBox.removeAll();
        schemeThemeComboBox.removeAll();
    }
}