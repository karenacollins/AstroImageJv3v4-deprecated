// Set_Debug.java

import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * Setup plug-in for debuging astroj.
 *
 * @author F.V. Hessman, Georg-August-Universitaet Goettingen
 * @version 1.0
 * @date 2008-Jun-25
 */
public class Set_Debug implements PlugIn {
    public void run(String arg) {
        boolean debug = Prefs.get("astroj.debug", false);

        GenericDialog gd = new GenericDialog("Debug Options");
        gd.addCheckbox("Debug", debug);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        debug = gd.getNextBoolean();
        Prefs.set("astroj.debug", debug);
    }
}
