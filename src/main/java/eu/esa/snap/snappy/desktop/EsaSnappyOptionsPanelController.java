package eu.esa.snap.snappy.desktop;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;

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
    @Override
    public void update() {

    }

    @Override
    public void applyChanges() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public JComponent getComponent(Lookup lookup) {
        return new JPanel();
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
