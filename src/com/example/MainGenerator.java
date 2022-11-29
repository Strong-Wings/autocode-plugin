package com.example;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainGenerator extends AnAction
{
    private static final Map<String, String> MAP_OF_FILES = Map.of(
            "%ucsRepository.java",
            "public class %ucsRepository\n" +
                    "{\n" +
                    "   public %ucsRepository()\n" +
                    "   {\n" +
                    "   }\n" +
                    "}\n",
            "%ucsServiceImpl.java",
            "public class %ucsServiceImpl implements %ucsService\n" +
                    "{\n" +
                    "   private final %ucsRepository _%lcsRepository;\n" +
                    "   public %ucsServiceImpl(%ucsRepository %lcsRepository)\n" +
                    "   {\n" +
                    "       _%lcsRepository = %lcsRepository;\n" +
                    "   }\n" +
                    "}\n",
            "%ucsService.java",
            "public interface %ucsService\n" +
                    "{\n" +
                    "}\n",
            "%ucsController.java",
            "public class %ucsController\n" +
                    "{\n" +
                    "   private final %ucsService _%lcsService;\n" +
                    "   public %ucsController(%ucsService %lcsService)\n" +
                    "   {\n" +
                    "       _%lcsService = %lcsService;\n" +
                    "   }\n" +
                    "}\n"
    );

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        var project = e.getProject();
        var dialog = new SampleDialogWrapper(project);
        if (dialog.showAndGet())
        {
            var value = dialog.getValue();
            var vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (vf != null)
            {
                var virtDir = vf.isDirectory() ? vf : vf.getParent();
                var java = Language.findLanguageByID("JAVA");
                var application = ApplicationManager.getApplication();
                for (var entry : MAP_OF_FILES.entrySet())
                {
                    var name = entry.getKey()
                            .replaceAll("%ucs", StringUtils.capitalize(value))
                            .replaceAll("%lcs", value.toLowerCase());
                    var code = entry.getValue()
                            .replaceAll("%ucs", StringUtils.capitalize(value))
                            .replaceAll("%lcs", value.toLowerCase());
                    application.runWriteAction(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                var psif = PsiFileFactory.getInstance(project).createFileFromText(name, java, code);
                                var psidir = PsiDirectoryFactory.getInstance(project).createDirectory(virtDir);
                                psidir.add(psif);
                            });
                        }
                    });
                }
            }
        }
    }

    public static class SampleDialogWrapper extends DialogWrapper
    {
        JTextField jTextField;
        String value;

        public SampleDialogWrapper(Project project)
        {
            super(project);
            setTitle("Input name");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel()
        {
            JPanel dialogPanel = new JPanel(new BorderLayout());
            this.jTextField = new JTextField();
            dialogPanel.add(jTextField, BorderLayout.PAGE_START);
            return dialogPanel;
        }

        @Override
        public boolean showAndGet()
        {
            if (!this.isModal())
            {
                throw new IllegalStateException("The showAndGet() method is for modal dialogs only");
            } else
            {
                this.show();
                this.value = this.jTextField.getText();
                return this.isOK();
            }
        }

        public String getValue()
        {
            return this.value;
        }
    }
}
