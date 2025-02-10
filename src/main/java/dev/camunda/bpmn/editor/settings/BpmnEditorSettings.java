package dev.camunda.bpmn.editor.settings;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static java.util.Objects.nonNull;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.serviceContainer.NonInjectable;
import dev.camunda.bpmn.editor.ui.component.panel.EnginePanel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A class that manages the settings for the BPMN Editor. This class is responsible for
 * persisting and loading the editor settings, such as the color theme, engine, and script type.
 *
 * <p>The settings are stored in an XML file and can be accessed and modified through this class.</p>
 *
 * @author Oleksandr Havrysh
 */
@Data
@NoArgsConstructor(onConstructor_ = {@NonInjectable})
@State(name = "dev.camunda.bpmn.editor.settings.BpmnEditorSettings", storages = @Storage("BpmnEditorSettings.xml"))
public final class BpmnEditorSettings implements PersistentStateComponent<BpmnEditorSettings.State> {

    /**
     * A class representing the state of the BPMN Editor settings.
     */
    @Data
    public static class State {

        @NotNull
        private ColorTheme colorTheme = ColorTheme.DARK;

        private Engine engine;
        private ScriptType scriptType;
        private SchemaTheme schemaTheme = SchemaTheme.DEFAULT;
        private Boolean useBpmnLinter = true;
        private Boolean useToolBar = true;

        @NotNull
        private Map<String, FileSettings> fileSettings = new HashMap<>();

        /**
         * Adds file settings for a specific file.
         *
         * @param fileName     The name of the file
         * @param engineResult The engine result containing the engine and whether to save it as default
         */
        public void addFileSettings(@NotNull String fileName, @NotNull EnginePanel.EngineResult engineResult) {
            var value = new FileSettings();
            value.setEngine(engineResult.engine());
            fileSettings.put(fileName, value);

            if (engineResult.saveAsDefault()) {
                engine = engineResult.engine();
            }

            getApplication().saveSettings();
        }

        /**
         * Returns the engine for a specific file path.
         *
         * @param path The file path
         * @return The engine for the file path
         */
        public @NotNull String getEngine(String path) {
            return Optional.ofNullable(fileSettings.get(path))
                    .map(BpmnEditorSettings.FileSettings::getEngine)
                    .or(() -> Optional.ofNullable(engine))
                    .map(BpmnEditorSettings.Engine::getValue)
                    .orElse("");
        }

        /**
         * Returns the color theme for a specific file path.
         *
         * @param path The file path
         * @return The color theme for the file path
         */
        public @NotNull String getColorTheme(String path) {
            return Optional.ofNullable(fileSettings.get(path))
                    .map(BpmnEditorSettings.FileSettings::getColorTheme)
                    .orElse(colorTheme)
                    .name();
        }

        /**
         * Returns the script type for a specific file path.
         *
         * @param path The file path
         * @return The script type for the file path
         */
        public @NotNull String getScriptType(String path) {
            return Optional.ofNullable(fileSettings.get(path))
                    .map(BpmnEditorSettings.FileSettings::getScriptType)
                    .or(() -> Optional.ofNullable(scriptType))
                    .map(BpmnEditorSettings.ScriptType::getValue)
                    .orElse("");
        }

        public @NotNull String getSchemaTheme(String path) {
            return Optional.ofNullable(fileSettings.get(path))
                    .map(BpmnEditorSettings.FileSettings::getSchemaTheme)
                    .or(() -> Optional.ofNullable(schemaTheme))
                    .map(BpmnEditorSettings.SchemaTheme::name)
                    .orElse("");
        }

        /**
         * Checks if the engine is set in the BPMN editor settings.
         *
         * @param path The file path
         * @return true if the engine is set, false otherwise
         */
        public boolean isEngineSet(String path) {
            return nonNull(getEngine()) || (getFileSettings().containsKey(path));
        }
    }

    @NotNull
    private State state = new State();

    /**
     * Returns the instance of the BPMN Editor settings.
     *
     * @return The instance of the BPMN Editor settings
     */
    public static @NotNull BpmnEditorSettings getInstance() {
        return Optional.ofNullable(getApplication().getService(BpmnEditorSettings.class))
                .orElseGet(BpmnEditorSettings::new);
    }

    /**
     * Loads the given state into the BPMN Editor settings.
     *
     * @param state The state to be loaded
     */
    @Override
    public void loadState(@NotNull BpmnEditorSettings.State state) {
        this.state = state;
    }

    /**
     * An enum representing the color theme of the BPMN Editor.
     */
    @Getter
    @AllArgsConstructor
    public enum ColorTheme {

        LIGHT("Light"),
        DARK("Dark"),
        DRACULA("Dracula");

        private final String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * An enum representing the script type of the BPMN Editor.
     */
    @Getter
    @AllArgsConstructor
    public enum ScriptType {

        GROOVY("Groovy", "groovy"),
        JAVASCRIPT("JavaScript", "javascript");

        private final String name;
        private final String value;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * An enum representing the engine of the BPMN Editor.
     */
    @Getter
    @AllArgsConstructor
    public enum Engine {

        CLASSIC_BPMN("Classic BPMN", "cb"),
        CAMUNDA_7("Camunda 7", "c7"),
        CAMUNDA_8("Camunda 8", "c8");

        private final String name;
        private final String value;

        @Override
        public String toString() {
            return name;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum SchemaTheme {

        DEFAULT("Default"),
        SKETCHY("Sketchy");

        private final String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * A class representing the file settings for the BPMN Editor.
     */
    @Data
    public static class FileSettings {
        private Engine engine;
        private ColorTheme colorTheme;
        private ScriptType scriptType;
        private SchemaTheme schemaTheme;
    }
}