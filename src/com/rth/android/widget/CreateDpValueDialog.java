package com.rth.android.widget;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.rth.android.util.Utils;
import com.rth.android.widget.interfaces.CreateDpValueActionListener;

import javax.swing.*;
import javax.swing.text.StringContent;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by rth on 16-10-31.
 */
public class CreateDpValueDialog extends JFrame implements ItemListener{

    private PsiFile[] dimenFiles;
    private JPanel contentJpanel;
    private CreateDpValueActionListener actionListener;
    private PsiFile selectDimenFile;
    private JTextField valueInput;

    public CreateDpValueDialog(PsiFile[] files, CreateDpValueActionListener actionListener) {
        this.actionListener = actionListener;
        this.dimenFiles = files;
        this.contentJpanel = new JPanel();
        this.contentJpanel.setLayout(new BoxLayout(contentJpanel,BoxLayout.Y_AXIS));
        setTitle("New DimensionValue");
        setPreferredSize(new Dimension(500, 220));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addValueInput();
        addPsiFiles();
        addDescription();
        addButtons();

        getContentPane().add(contentJpanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void addButtons() {
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120,35));
        btnPanel.add(btnCancel);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JButton btnSure = new JButton("Sure");
        btnSure.setPreferredSize(new Dimension(120,35));
        btnSure.setForeground(JBColor.blue);
        btnPanel.add(btnSure);

        ActionListener sureListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dpValue = valueInput.getText();
                if (dpValue == null || dpValue.length() == 0) {
                    Utils.showNotification(selectDimenFile.getProject(), MessageType.ERROR,"please input dp value!");
                    return;
                }
                if (dpValue.endsWith("dip")) {
                    Utils.showNotification(selectDimenFile.getProject(), MessageType.ERROR,"please use dp not dip!");
                    return;
                }
                if(!Utils.isCorrectDpInput(dpValue)) {
                    Utils.showNotification(selectDimenFile.getProject(), MessageType.ERROR,"please input correctly! such as 10dp");
                    return;
                }
                dispose();
                actionListener.selectDimenFile(dpValue, selectDimenFile, dimenFiles);
            }
        };
        btnSure.addActionListener(sureListener);
        btnSure.registerKeyboardAction(sureListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        contentJpanel.add(btnPanel);
    }

    private void addDescription() {
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        descPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel labelDesc = new JLabel("After choose, the value will be added into other files automaticaly.");
        labelDesc.setPreferredSize(new Dimension(450,35));
        descPanel.add(labelDesc);

        contentJpanel.add(descPanel);
    }

    private void addPsiFiles() {
        JPanel panelFiles = new JPanel();
        panelFiles.setLayout(new BoxLayout(panelFiles, BoxLayout.X_AXIS));
        panelFiles.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel labelSelectDirectory = new JLabel("Select the directory you want to insert : ");
        panelFiles.add(labelSelectDirectory);

        ComboBox comboBox = new ComboBox();
        for (int i = dimenFiles.length-1; i >= 0; i--) {
            PsiFile dimenFile = dimenFiles[i];
            PsiDirectory directory = dimenFile.getParent();
            if (directory != null) {
                if("values".equals(directory.getName())) {
                    continue;
                }
                if (selectDimenFile == null) selectDimenFile = dimenFile;
                comboBox.addItem(directory.getName());
            }
        }
        comboBox.addItemListener(this);
        panelFiles.add(comboBox);
        panelFiles.setPreferredSize(new Dimension(450,40));

        contentJpanel.add(panelFiles);
    }

    private void addValueInput() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel labelInput = new JLabel("Resource Value : ");
        jPanel.add(labelInput);

        jPanel.add(Box.createHorizontalStrut(20));

        valueInput = new JTextField(1);
        jPanel.add(valueInput);
        valueInput.setPreferredSize(new Dimension(450,40));

        contentJpanel.add(jPanel);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        final int state = e.getStateChange();
        if(state == ItemEvent.SELECTED) {
            String selectDirectoryName = e.getItem().toString();
            for (PsiFile dimenFile : dimenFiles) {
                if (dimenFile.getParent() != null) {
                    if (dimenFile.getParent().getName().equals(selectDirectoryName)) {
                        selectDimenFile = dimenFile;
                        return;
                    }
                }
            }
        }
    }
}
