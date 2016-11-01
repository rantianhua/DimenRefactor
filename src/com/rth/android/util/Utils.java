package com.rth.android.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.awt.RelativePoint;

/**
 * Created by rth on 16-10-31.
 */
public class Utils {

    private static StringBuilder stringBuilder = new StringBuilder();

    /**
     * find all dimens.xml in project
     * @param element in caret
     * @return founded dimens.xml in project
     */
    public static PsiFile[] findAllDimenFiles(PsiElement element) {
        Project project = element.getProject();
        Module module = ModuleUtil.findModuleForPsiElement(element);
        if (module == null) return null;
        GlobalSearchScope scope = module.getModuleWithDependenciesScope();
        return FilenameIndex.getFilesByName(project,"dimens.xml",scope);
    }

    /**
     * show notification
     * @param project
     * @param type
     * @param text
     */
    public static void showNotification(Project project, MessageType type, String text) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atLeft);
    }

    /**
     * get number in PsiDirectory name
     * for example :
     * this method will return 360 if PsiDirectory's name is values-w360dp
     * @return
     */
    public static int getDpNumInPsiFile(PsiFile psiFile) {
        if (psiFile == null) return -1;
        PsiDirectory directory = psiFile.getParent();
        if (directory == null) return -1;
        String name = directory.getName();
        if(!name.equals("values")) {
            return Integer.valueOf(name.substring(8,name.indexOf("dp")));
        }
        return 0;
    }

    /**
     * generate dimen element
     * @param dpName
     * @param dpValue
     * @param dimenFile
     * @param defDimenFile
     * @return
     */
    public static String generateDimenText(String dpName, String dpValue, PsiFile dimenFile, PsiFile defDimenFile) {
        stringBuilder.delete(0,stringBuilder.length());
        stringBuilder
                .append("\n")
                .append("\t")
                .append("<dimen name=\"")
                .append(dpName)
                .append("\"")
                .append(">");
        int dimenNum = getDpNumInPsiFile(dimenFile);
        int defDimenNum = getDpNumInPsiFile(defDimenFile);
        if (dimenNum == -1) return null;
        if (dimenNum == 0) dimenNum = 320;
        if (defDimenNum == 0) defDimenNum = 320;
        int dpValueInt = 0;
        try {
            dpValueInt = Integer.valueOf(dpValue.substring(0,dpValue.indexOf("dp")));
        } catch (NumberFormatException e) {
            return null;
        }
        int scaleDpValue = Math.round(dimenNum * 1.0f / defDimenNum * dpValueInt);
        dpValue = scaleDpValue + "dp";
        stringBuilder.append(dpValue);
        stringBuilder.append("</dimen>");
        return stringBuilder.toString();
    }

    /**
     * validate whether have correct input or not
     * @param dpValue
     * @return
     */
    public static boolean isCorrectDpInput(String dpValue) {
        try {
            int value = Integer.valueOf(dpValue.substring(0,dpValue.indexOf("dp")));
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String askNewDpValue(Project project) {
        return Messages.showInputDialog(project,"input yout new value : ","refactor dp value",Messages.getInformationIcon());
    }

    /**
     * get modify offset, for Document.replace()
     * @return
     */
    public static Pair<Integer, Integer> getModifyOffset(Document document, String dimeName) {
        int lineCount = document.getLineCount();
        for (int i = lineCount-1; i > 1;i--) {
            int lineEndOffset = document.getLineEndOffset(i);
            int lineEndOffsetInPre = document.getLineEndOffset(i-1);
            TextRange textRange = new TextRange(lineEndOffsetInPre + 1,lineEndOffset);
            String lineText = document.getText(textRange);
            if (lineText.length() == 0 || !lineText.contains(dimeName)) {
                continue;
            }
            return new Pair<>(lineEndOffsetInPre,lineEndOffset);
        }
        return null;
    }

    /**
     * get insert offset, for Document.insert()
     * @param document
     * @return
     */
    public static int getInsertOffset(Document document) {
        int lineCount = document.getLineCount();
        for (int i = lineCount - 1;i >= 1;i--) {
            int lineEndOffset = document.getLineEndOffset(i);
            int preLineEndOffset = document.getLineEndOffset(i-1);
            TextRange textRange = new TextRange(preLineEndOffset + 1,lineEndOffset);
            String text = document.getText(textRange);
            if("</resources>".equals(text)) {
                //this line is last line
                return preLineEndOffset;
            }
        }
        return -1;
    }

    /**
     * check the dp wether in dimens.xml or not
     * @param document
     * @param dpName
     * @return
     */
    public static boolean checkDpInDimenFile(Document document, String dpName) {
        return document.getText().contains(dpName);
    }
}
