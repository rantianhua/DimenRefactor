package com.rth.android.dimenrefactor;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.rth.android.util.Utils;
import com.rth.android.widget.CreateDpValueDialog;
import com.rth.android.widget.interfaces.CreateDpValueActionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by rth on 16-10-30.
 */
public class DimenAction extends BaseGenerateAction implements CreateDpValueActionListener {

    private static final String FILE_DIMEN = "dimens.xml";
    private static final String DIRECTORY_LAYOUT = "layout";

    private Editor editor;
    private Project project;
    private String dimenName;
    private PsiFile editingPsiFile;

    public DimenAction() {
        super(null);
    }

    public DimenAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        editingPsiFile = e.getData(DataKeys.PSI_FILE);
        editor = e.getData(DataKeys.EDITOR);
        project = e.getData(DataKeys.PROJECT);
        if (editor == null) return;
        if (project == null) return;
        if (editingPsiFile == null) return;
        if (isDimenFile(editingPsiFile)) {
            //operate dimen.xml
            operateDimenFile(e);
        }else if (isLayoutFile(editingPsiFile)){
            //operate layout xml file
            operateLayoutXmlFile(editingPsiFile);
        }
    }

    private void operateLayoutXmlFile(PsiFile psiFile) {
        CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        PsiElement psiElement = psiFile.findElementAt(caretOffset);
        if (psiElement == null) return;
        String text = psiElement.getText();
        if(!text.startsWith("@dimen/")) {
            psiElement = psiFile.findElementAt(caretOffset -1 );
            if (psiElement == null) return;
            text = psiElement.getText();
            if (!text.startsWith("@dimen/")) return;
        }
        text = text.substring(text.indexOf("/") + 1);

        PsiFile[] dimenFiles= Utils.findAllDimenFiles(psiElement);
        if (dimenFiles == null || dimenFiles.length < 2) return;
        dimenName = text;

        CreateDpValueDialog dpValueDialog = new CreateDpValueDialog(dimenFiles,this);
        dpValueDialog.setVisible(true);
    }

    private void operateDimenFile(AnActionEvent e) {
        PsiElement psiElement = e.getData(DataKeys.PSI_ELEMENT);
        if (psiElement == null) return;
        String psiText = psiElement.getText();
        if (psiText == null || psiText.length() == 0) return;
        if (psiText.startsWith("<dimen name=")) {
            psiText = psiText.substring(psiText.indexOf("\"") + 1);
            psiText = psiText.substring(0,psiText.indexOf("\""));
        }
        psiText = psiText.replaceAll("\"","");

        String newDpValue = Utils.askNewDpValue(project);
        if (newDpValue == null || newDpValue.length() == 0) {
            return;
        }
        if (!Utils.isCorrectDpInput(newDpValue)) {
            Utils.showNotification(project, MessageType.ERROR,"please input correctly! such as 10dp");
            return;
        }
        PsiFile[] dimenFiles = Utils.findAllDimenFiles(psiElement);
        if (dimenFiles == null || dimenFiles.length < 2) return;

        new DimenWriter(project,newDpValue, psiText,editingPsiFile, DimenWriter.DimenMode.MODIFY, dimenFiles).execute();
    }

    @Override
    protected void update(@NotNull Presentation presentation, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull DataContext dataContext, @Nullable String actionPlace) {
        presentation.setEnabledAndVisible(isDimenFile(file) || isLayoutFile(file));
    }

    private boolean isDimenFile(PsiFile file) {
        return file != null && FILE_DIMEN.equals(file.getName());
    }

    private boolean isLayoutFile(PsiFile file) {
        if (file == null) return false;
        PsiDirectory directory = file.getParent();
        return directory != null && directory.getName().endsWith(DIRECTORY_LAYOUT);
    }

    @Override
    public void selectDimenFile(String dpValue, PsiFile psiFile, PsiFile[] dimenFiles) {
        new DimenWriter(project, dpValue, dimenName, psiFile, DimenWriter.DimenMode.INSERT, dimenFiles).execute();
    }
}
