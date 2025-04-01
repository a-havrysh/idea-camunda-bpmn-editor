package dev.camunda.bpmn.editor.vfs;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BpmnFileType extends LanguageFileType {

    public BpmnFileType() {
        super(BpmnLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "BPMN";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "BPMN process file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "bpmn";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return BpmnIcons.LOGO;
    }

    public static class BpmnLanguage extends Language {

        public static final BpmnLanguage INSTANCE = new BpmnLanguage();

        private BpmnLanguage() {
            super("BPMN");
        }
    }

    public interface BpmnIcons {
        Icon LOGO = IconLoader.getIcon("/icons/filelogo.svg", BpmnIcons.class);
    }
}
