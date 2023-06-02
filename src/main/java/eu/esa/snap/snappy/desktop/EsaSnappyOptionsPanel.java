/*
 * Copyright (c) 2021.  Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package eu.esa.snap.snappy.desktop;

//import org.esa.snap.rcp.SnapApp;

import org.esa.snap.runtime.Config;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

final class EsaSnappyOptionsPanel extends javax.swing.JPanel {


    private final EsaSnappyOptionsPanelController controller;

    private javax.swing.JPanel snappyParametersPanel;

    private JComponent pythonPathLabel;
    private javax.swing.JTextField pythonPathTextField;
    private javax.swing.JButton browseUserDirButton;


    EsaSnappyOptionsPanel(EsaSnappyOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        addListenerToComponents(controller);
    }

    void load() {
        Preferences preferences = Config.instance("snap").load().preferences();

        // todo: implement. see e.g. WriterPanel in Znap UI
    }

    void store() {
        Preferences preferences = Config.instance("snap").load().preferences();

        // todo: implement. see e.g. WriterPanel in Znap UI
    }

   
    boolean valid() {
        return true;
    }

    private void initComponents() {

        GridBagConstraints gridBagConstraints;

        snappyParametersPanel = new javax.swing.JPanel();

        snappyParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("ESA Snappy Options"));
        snappyParametersPanel.setMinimumSize(new java.awt.Dimension(283, 115));
        snappyParametersPanel.setLayout(new java.awt.GridBagLayout());

        pythonPathLabel = new JLabel("Path to Python executable:");
        org.openide.awt.Mnemonics.setLocalizedText((JLabel) pythonPathLabel, "Path to Python executable");
        pythonPathLabel.setMaximumSize(new java.awt.Dimension(100, 14));
        pythonPathLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        snappyParametersPanel.add(pythonPathLabel, gridBagConstraints);

        pythonPathTextField = new JTextField("todo");

        pythonPathTextField.setText("todo");
        pythonPathTextField.setToolTipText("todo");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        snappyParametersPanel.add(pythonPathTextField, gridBagConstraints);

        browseUserDirButton = new javax.swing.JButton();
        org.openide.awt.Mnemonics.setLocalizedText(browseUserDirButton, "...");
        browseUserDirButton.addActionListener(evt -> browsePythonPathButtonActionPerformed());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 10);
        snappyParametersPanel.add(browseUserDirButton, gridBagConstraints);

//        setBorder(new TitledBorder("ESA Snappy Options"));
//        GroupLayout layout = new GroupLayout(this);
//        layout.setAutoCreateGaps(true);
//        layout.setAutoCreateContainerGaps(true);
//        setLayout(layout);

        add(snappyParametersPanel);
    }

    private void browsePythonPathButtonActionPerformed() {
        JFileChooser fileChooser = new JFileChooser(pythonPathTextField.getText());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            pythonPathTextField.setText(selectedDir.getAbsolutePath());
            pythonPathTextField.setForeground(Color.BLACK);
            controller.changed();
        }
    }

    private void addListenerToComponents(EsaSnappyOptionsPanelController controller) {
       // todo
    }

    private void updateState() {
       // todo
    }
}
