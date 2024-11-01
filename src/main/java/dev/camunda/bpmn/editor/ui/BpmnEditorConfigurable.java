package dev.camunda.bpmn.editor.ui;

import com.intellij.openapi.options.Configurable;
import dev.camunda.bpmn.editor.config.BpmnEditorSettings;
import dev.camunda.bpmn.editor.ui.component.JBpmnEditorComponent;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * A configurable settings panel for the BPMN Editor. This class implements the {@link Configurable}
 * interface to provide a custom settings UI for the BPMN Editor within the IntelliJ IDEA settings dialog.
 *
 * <p>The settings panel allows users to configure the color theme of the BPMN Editor. The settings
 * are persisted using the {@link BpmnEditorSettings} class.</p>
 *
 * @author Oleksandr Havrysh
 */
public class BpmnEditorConfigurable implements Configurable {

    private static final String BPMN_EDITOR_SETTINGS = "Camunda BPMN Editor Settings";

    private JBpmnEditorComponent bpmnEditorComponent;

    /**
     * Returns the display name of the settings panel.
     *
     * @return The display name of the settings panel
     */
    @Nls
    @Override
    public @NotNull String getDisplayName() {
        return BPMN_EDITOR_SETTINGS;
    }

    /**
     * Returns the preferred component to focus when the settings panel is opened.
     *
     * @return The preferred component to focus
     */
    @Override
    public @NotNull JComponent getPreferredFocusedComponent() {
        return Optional.ofNullable(bpmnEditorComponent)
                .map(JBpmnEditorComponent::getPreferredFocusedComponent)
                .orElse(new JPanel());
    }

    /**
     * Creates and returns the main component of the settings panel.
     *
     * @return The main component of the settings panel
     */
    @Override
    public @NotNull JComponent createComponent() {
        bpmnEditorComponent = new JBpmnEditorComponent();
        return bpmnEditorComponent;
    }

    /**
     * Checks if the settings have been modified.
     *
     * @return true if the settings have been modified, false otherwise
     */
    @Override
    public boolean isModified() {
        var state = BpmnEditorSettings.getInstance().getState();
        return Optional.ofNullable(bpmnEditorComponent)
                .map(component -> component.getColorThemeValue() != state.getColorTheme()
                        || component.getScriptTypeValue() != state.getScriptType())
                .orElse(false);
    }

    /**
     * Applies the modified settings.
     */
    @Override
    public void apply() {
        var state = BpmnEditorSettings.getInstance().getState();
        Optional.ofNullable(bpmnEditorComponent).ifPresent(component -> {
            state.setColorTheme(component.getColorThemeValue());
            state.setScriptType(component.getScriptTypeValue());
        });
    }

    /**
     * Resets the settings to their original values.
     */
    @Override
    public void reset() {
        var state = BpmnEditorSettings.getInstance().getState();
        Optional.ofNullable(bpmnEditorComponent).ifPresent(component -> {
            component.setColorThemeValue(state.getColorTheme());
            component.setScriptTypeValue(state.getScriptType());
        });
    }

    /**
     * Disposes of the UI resources used by the settings panel.
     */
    @Override
    public void disposeUIResources() {
        bpmnEditorComponent = null;
    }
}