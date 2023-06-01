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
import java.util.prefs.Preferences;

final class EsaSnappyOptionsPanel extends javax.swing.JPanel {


    private final EsaSnappyOptionsPanelController controller;

    private javax.swing.JTextField pythonPathTextField;
    private JComponent pythonPathLabel;

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
        pythonPathLabel = new JLabel("Path to Python executable:");
        pythonPathTextField = new JTextField("");

        setBorder(new TitledBorder("ESA Snappy Options"));
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        setLayout(layout);
//        layout.setVerticalGroup(
//                layout.createSequentialGroup()
//                        .addGroup(
//                                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                                        .addComponent(createZipArchiveLabel)
//                                        .addComponent(createZipArchiveCheck))
//                        .addGroup(
//                                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                                        .addComponent(binaryFormatLabel)
//                                        .addComponent(binaryFormatCombo))
//                        .addGroup(
//                                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                                        .addComponent(compressorLabel)
//                                        .addComponent(compressorCombo))
//                        .addGroup(
//                                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                                        .addComponent(compressionLevelLabel)
//                                        .addComponent(compressionLevelCombo))
//                        .addGap(0, 2000, Short.MAX_VALUE)
//        );
//        layout.setHorizontalGroup(
//                layout.createSequentialGroup()
//                        .addGroup(
//                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(binaryFormatLabel)
//                                        .addComponent(compressorLabel)
//                                        .addComponent(compressionLevelLabel)
//                                        .addComponent(createZipArchiveLabel))
//                        .addGroup(
//                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(binaryFormatCombo)
//                                        .addComponent(compressorCombo)
//                                        .addComponent(compressionLevelCombo)
//                                        .addComponent(createZipArchiveCheck))
//                        .addGap(0, 2000, Short.MAX_VALUE)
//        );

    }

    private void addListenerToComponents(EsaSnappyOptionsPanelController controller) {
       // todo
    }

    private void updateState() {
       // todo
    }
}
