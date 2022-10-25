package org.esa.snap.snappy.desktop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;


public class ShowSnappySettingsAction extends AbstractAction {
    public static final String KEY_DISPLAY_NAME = "displayName";
    public static final String KEY_DESCRIPTION = "description";

    /**
     * Action factory method used in NetBeans {@code layer.xml} file, e.g.
     *
     * @param configuration Configuration attributes from layer.xml.
     * @return The action.
     */
    public static ShowSnappySettingsAction create(Map<String, Object> configuration) {
        ShowSnappySettingsAction action = new ShowSnappySettingsAction();
        action.putValue(NAME, configuration.get(KEY_DISPLAY_NAME));
        action.putValue(SHORT_DESCRIPTION, KEY_DESCRIPTION);
        return action;

    }
    /**
     * Launches configuration wizard for ESA snappy.
     *
     * @param event the command event.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Window window = SwingUtilities.windowForComponent((Component) event.getSource());
        JDialog dialog = new JDialog(window, "ESA Snappy Settings", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLocationRelativeTo(window);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.setContentPane(new JLabel("This shows the ESA Snappy settings!"));
        dialog.pack();
        dialog.setVisible(true);

    }
}
