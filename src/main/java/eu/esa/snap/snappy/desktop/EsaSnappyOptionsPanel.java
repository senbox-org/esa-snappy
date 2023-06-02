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

import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.runtime.Config;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

final class EsaSnappyOptionsPanel extends  JPanel {


    private final EsaSnappyOptionsPanelController controller;

    private  JTextField pythonPathTextField;
    private  JTextField esaSnappyDirTextField;


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

        setBorder(new TitledBorder("ESA Snappy Configuration Parameters"));

        BoxLayout perfPanelLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(perfPanelLayout);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        final String userHomePath = SystemUtils.getUserHomeDir().getAbsolutePath();

        JPanel snappyParametersPanel = new JPanel();
        final int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        snappyParametersPanel.setMaximumSize(new Dimension(new Dimension(screenWidth, 150)));
        snappyParametersPanel.setLayout(new GridBagLayout());

        JComponent pythonPathLabel = new JLabel("Path to Python executable:");
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 10, 0, 0);
        snappyParametersPanel.add(pythonPathLabel, gridBagConstraints);

        pythonPathTextField = new JTextField();
        pythonPathTextField.setText("");
        pythonPathTextField.setToolTipText("Select path to Python executable");
        gridBagConstraints = new  GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new  Insets(5, 0, 0, 0);
        snappyParametersPanel.add(pythonPathTextField, gridBagConstraints);

        JButton browsePythonPathButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(browsePythonPathButton, "...");
        browsePythonPathButton.addActionListener(evt -> browsePythonPathButtonActionPerformed());
        gridBagConstraints = new  GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 10);
        snappyParametersPanel.add(browsePythonPathButton, gridBagConstraints);


        JComponent esaSnappyDirLabel = new JLabel("Installation directory of Python 'esa_snappy' package:");
        gridBagConstraints = new  GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        snappyParametersPanel.add(esaSnappyDirLabel, gridBagConstraints);

        esaSnappyDirTextField = new JTextField();
        final String esaSnappyDefaultDir = userHomePath + File.separator + ".snap" + File.separator + "snap-python";
        esaSnappyDirTextField.setText(esaSnappyDefaultDir);
        esaSnappyDirTextField.setToolTipText("Select installation directory of Python 'esa_snappy' package");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new  Insets(5, 0, 0, 0);
        snappyParametersPanel.add(esaSnappyDirTextField, gridBagConstraints);

        JButton browseEsaSnappyDirButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(browseEsaSnappyDirButton, "...");
        browseEsaSnappyDirButton.addActionListener(evt -> browseEsaSnappyDirButtonActionPerformed());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 10);
        snappyParametersPanel.add(browseEsaSnappyDirButton, gridBagConstraints);

        add(snappyParametersPanel);

    }

    private void browsePythonPathButtonActionPerformed() {
        JFileChooser fileChooser = new JFileChooser(pythonPathTextField.getText());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            pythonPathTextField.setText(selectedDir.getAbsolutePath());
            pythonPathTextField.setForeground(Color.BLACK);
            controller.changed();
        }
    }

    private void browseEsaSnappyDirButtonActionPerformed() {
        JFileChooser fileChooser = new JFileChooser(esaSnappyDirTextField.getText());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            esaSnappyDirTextField.setText(selectedDir.getAbsolutePath());
            esaSnappyDirTextField.setForeground(Color.BLACK);
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
