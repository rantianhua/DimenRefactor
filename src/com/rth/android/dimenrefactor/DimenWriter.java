package com.rth.android.dimenrefactor;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.rth.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rth on 16-10-31.
 */
public class DimenWriter extends WriteCommandAction.Simple {

    private PsiFile[] psiFiles; //all dimens.xml in project
    private String dpValue; //new dimen value
    private String dpName; //dimen name, will be added or modified
    private PsiFile editingPsiFile; //the file in editor
    private Project project;
    private int dimenMode;

    protected DimenWriter(Project project, String dpValue, String dpName, PsiFile editingFile, int dimenMode, PsiFile... files) {
        super(project, files);
        this.project = project;
        this.psiFiles = files;
        this.dpValue = dpValue;
        this.dpName = dpName;
        this.dimenMode = dimenMode;
        this.editingPsiFile = editingFile;
    }

    @Override
    protected void run() throws Throwable {
        switch (this.dimenMode) {
            case DimenMode.INSERT:
                insertDimen();
                break;
            case DimenMode.MODIFY:
                modifyDimen();
                break;
        }
    }

    private void modifyDimen() {
        List<DocumentWrapper> documentsModify = new ArrayList<>();
        for (PsiFile dimenFile : psiFiles) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(dimenFile);
            if (document == null) {
                System.out.println("documen is null in " + dimenFile.getViewProvider().toString());
                return;
            }
            if (!Utils.checkDpInDimenFile(document,dpName)) {
                System.out.println("no dp value in this file");
                return;
            }
            Pair<Integer,Integer> modifyOffset = Utils.getModifyOffset(document, dpName);
            if (modifyOffset == null) return;
            String dimenText = Utils.generateDimenText(dpName,dpValue,dimenFile,editingPsiFile);
            if (dimenText == null) return;
            documentsModify.add(new DocumentWrapper(document,dimenText,0,modifyOffset));
        }
        if (documentsModify.size() != psiFiles.length) {
            //any exception occurs, do not modify
            return;
        }
        for (DocumentWrapper documentWrapper : documentsModify) {
            documentWrapper.modifyDpValueIntoDocument();
        }
    }

    private void insertDimen() {
        List<DocumentWrapper> documentWrappers = new ArrayList<>();
        for (PsiFile dimenFile : psiFiles) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(dimenFile);
            if (document == null) {
                System.out.println("documen is null in " + dimenFile.getViewProvider().toString());
                return;
            }
            if (Utils.checkDpInDimenFile(document,dpName)) {
                System.out.println("dp value has been in this file before");
                return;
            }
            int insertOffset = Utils.getInsertOffset(document);
            if (insertOffset == -1) {
                System.out.println("find insert offset failed");
                return;
            }
            String appendDimen = Utils.generateDimenText(dpName,dpValue,dimenFile,editingPsiFile);
            if (appendDimen == null) return;
            documentWrappers.add(new DocumentWrapper(document,appendDimen,insertOffset,null));
        }
        if (documentWrappers.size() != psiFiles.length) {
            //any exception occurs, do not insert
            return;
        }
        for (DocumentWrapper documentWrapper : documentWrappers) {
            documentWrapper.insertDpValueIntoDocument();
        }
    }

    static class DocumentWrapper {

        private Document document;
        private int insertOffset;
        private String dimenText;
        private Pair<Integer,Integer> modifyOffset;

        public DocumentWrapper(Document document, String dimenText, int insertOffset, Pair<Integer,Integer> modifyOffset) {
            this.document = document;
            this.insertOffset = insertOffset;
            this.dimenText = dimenText;
            this.modifyOffset = modifyOffset;
        }

        public Document getDocument() {
            return document;
        }

        public int getInsertOffset() {
            return insertOffset;
        }

        public String getDimenText() {
            return dimenText;
        }

        @Override
        public String toString() {
            return "insertOffset is " + insertOffset + "\n" + "append text is " + dimenText;
        }

        public void insertDpValueIntoDocument() {
            getDocument().insertString(getInsertOffset(), getDimenText());
        }

        public void modifyDpValueIntoDocument() {
            getDocument().replaceString(modifyOffset.first, modifyOffset.second, getDimenText());
        }
    }

    public interface DimenMode {
        int INSERT = 0;
        int MODIFY = 1;
    }
}
