package eu.esa.snap.snappy.desktop;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

@SuppressWarnings("unused")
@OptionsPanelController.TopLevelRegistration(
        id = "ESASNAPPY",
        categoryName = "ESA-Snappy",
        // todo - only placeholder image - create better image
        iconBase = "eu/esa/snap/snappy/desktop/snap-python32.png",
        keywords = "python, snappy",
        keywordsCategory = "ESA-Snappy",
        position = 2000
)
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
        // todo ?
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
        // todo
        return new HelpCtx("esaSnappyConfig");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        // todo ?
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        // todo ?
    }
}
