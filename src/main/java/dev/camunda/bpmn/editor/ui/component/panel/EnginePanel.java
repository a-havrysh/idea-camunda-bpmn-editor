package dev.camunda.bpmn.editor.ui.component.panel;

import static com.intellij.util.ui.JBUI.emptyInsets;
import static com.intellij.util.ui.JBUI.insetsBottom;
import static dev.camunda.bpmn.editor.settings.BpmnEditorSettings.Engine.CAMUNDA_7;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.util.Arrays.stream;

import com.intellij.openapi.ui.DescriptionLabel;
import dev.camunda.bpmn.editor.settings.BpmnEditorSettings;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * A UI component for selecting the BPMN engine.
 * This class provides a form with radio buttons to select the BPMN engine and a save button.
 * It also includes a checkbox to set the selected engine as the default for the next BPMN.
 *
 * @author Oleksandr Havrysh
 */
public class EnginePanel extends JPanel {

    private final AtomicReference<BpmnEditorSettings.Engine> engineRef = new AtomicReference<>(CAMUNDA_7);

    /**
     * Constructs a new EngineComponent.
     *
     * @param consumer The consumer to be notified when the save button is clicked
     */
    public EnginePanel(Consumer<EngineResult> consumer, Supplier<JComponent> panelSupplier) {
        super(new GridBagLayout());

        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = RELATIVE;
        gbc.anchor = CENTER;
        gbc.insets = insetsBottom(40);
        add(new JLabel("Select BPMN engine"), gbc);

        gbc.insets = emptyInsets();
        add(new DescriptionLabel("Select the BPMN engine to be used by the BPMN editor."), gbc);
        add(new DescriptionLabel("You need to select the engine only once for the file."), gbc);
        gbc.insets = insetsBottom(20);
        add(new DescriptionLabel("but you can always change it in the settings."), gbc);

        var buttonGroup = new ButtonGroup();
        stream(BpmnEditorSettings.Engine.values()).forEach(engine -> {
            var engineRadioButton = new JRadioButton(engine.toString());
            engineRadioButton.addActionListener(e -> {
                if (engineRadioButton.isSelected()) {
                    engineRef.set(engine);
                }
            });
            buttonGroup.add(engineRadioButton);
            add(engineRadioButton, gbc);
        });

        var saveButton = new JButton("Save");
        add(saveButton, gbc);

        var saveAsDefaultCheckBox = new JCheckBox("Set as default for next BPMN");
        add(saveAsDefaultCheckBox, gbc);

        saveButton.addActionListener(e -> {
            consumer.accept(new EngineResult(saveAsDefaultCheckBox.isSelected(), engineRef.get()));

            removeAll();
            setLayout(new BorderLayout());
            add(panelSupplier.get());
            revalidate();
            repaint();
        });
    }

    /**
     * A record representing the result of the engine selection.
     *
     * @param saveAsDefault Whether to save the selected engine as the default for the next BPMN
     * @param engine        The selected BPMN engine
     */
    public record EngineResult(boolean saveAsDefault, BpmnEditorSettings.Engine engine) {
    }
}