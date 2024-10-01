package eu.esa.snap.snappy;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Olaf Danne
 */
public class PyBridgeTest {

    @Test
    public void testEncodeJarPath() {
        final String prefix = "jar:";
        final String suffix = "!/";
        final String sep = File.separator;

        // test for default Windows SNAP installation dir
        String uriStringInput = "jar:file:///C:/Program Files/esa-snap/snap/modules/eu-esa-snap-esa-snappy.jar!/";
        String encodedJarPath = PyBridge.encodeJarPath(prefix, suffix, uriStringInput);
        assertNotNull(encodedJarPath);
        assertTrue(encodedJarPath.contains("C:" + sep + "Program Files" + sep + "esa-snap" + sep + "snap" + sep + "modules" + sep + "eu-esa-snap-esa-snappy.jar"));

        uriStringInput = "jar:file:///C:/Program%20Files/esa-snap/snap/modules/eu-esa-snap-esa-snappy.jar!/";
        encodedJarPath = PyBridge.encodeJarPath(prefix, suffix, uriStringInput);
        assertNotNull(encodedJarPath);
        assertTrue(encodedJarPath.contains("C:" + sep + "Program Files" + sep + "esa-snap" + sep + "snap" + sep + "modules" + sep + "eu-esa-snap-esa-snappy.jar"));

        // insert some weird chars, should still work
        uriStringInput = "jar:file:///C:/Prog_ram Files/../esa-snap/s{na}p/m^odules/eu-esa-sn`ap-esa-snappy.jar!/";

        encodedJarPath = PyBridge.encodeJarPath(prefix, suffix, uriStringInput);
        assertNotNull(encodedJarPath);
        assertTrue(encodedJarPath.contains("C:" + sep + "Prog_ram Files" + sep + ".." + sep + "esa-snap" + sep + "s{na}p" + sep + "m^odules" + sep + "eu-esa-sn`ap-esa-snappy.jar"));
    }
}
