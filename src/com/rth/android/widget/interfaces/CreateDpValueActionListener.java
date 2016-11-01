package com.rth.android.widget.interfaces;

import com.intellij.psi.PsiFile;

/**
 * Created by rth on 16-10-31.
 */
public interface CreateDpValueActionListener {

    /**
     * select a dimen directory to create dp value
     * @param dpValue the dp value
     * @param psiFile selected dimens.xml
     * @param dimenFiles all dimen files
     */
    void selectDimenFile(String dpValue, PsiFile psiFile, PsiFile[] dimenFiles);
}
