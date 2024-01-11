package eu.esa.snap.snappy.desktop;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

// To show the tab for ESA-SNAPPY in the Tools --> Options dialog, activate the registration block below.
// If needed, the option to configure esa_snappy from this ESA-Snappy options dialog is foreseen for a
// future SNAP release >= 11.

//@SuppressWarnings("unused")
//@OptionsPanelController.TopLevelRegistration(
//        id = "ESASNAPPY",
//        categoryName = "ESA-Snappy",
//        // todo - only placeholder image - create better image
//        iconBase = "eu/esa/snap/snappy/desktop/snap-python32.png",
//        keywords = "python, snappy",
//        keywordsCategory = "ESA-Snappy",
//        position = 2000
//)
public class EsaSnappyOptionsPanelController extends OptionsPanelController {

    private EsaSnappyOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            getPanel().store();
            changed = false;
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public JComponent getComponent(Lookup lookup) {

//        return new JPanel();
        return getPanel();
    }

    private EsaSnappyOptionsPanel getPanel() {
        if (panel == null) {
            panel = new EsaSnappyOptionsPanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {

    }
}
