package eu.esa.snap.snappy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Olaf Danne
 */
public class PyBridgeTest {

    @Test
    public void testEncodeJarPath() {
        final String prefix = "jar:";
        final String suffix = "!/";

        // test for default Windows SNAP installation dir
        String uriStringInput = "jar:file:///C:/Program Files/esa-snap/snap/modules/eu-esa-snap-esa-snappy.jar!/";
        String encodedJarPath = PyBridge.encodeJarPath(prefix, suffix, uriStringInput);
        assertNotNull(encodedJarPath);
        assertEquals("C:\\Program Files\\esa-snap\\snap\\modules\\eu-esa-snap-esa-snappy.jar", encodedJarPath);

        uriStringInput = "jar:file:///C:/Program%20Files/esa-snap/snap/modules/eu-esa-snap-esa-snappy.jar!/";
        encodedJarPath = PyBridge.encodeJarPath(prefix, suffix, uriStringInput);
        assertNotNull(encodedJarPath);
        assertEquals("C:\\Program Files\\esa-snap\\snap\\modules\\eu-esa-snap-esa-snappy.jar", encodedJarPath);

        // insert some weird chars, should still work
        uriStringInput =
                "jar:file:///C:/Prog_ram Files/../esa-snap/s{na}p/m^odules/eu-esa-sn`ap-esa-snappy.jar!/";

        encodedJarPath = PyBridge.encodeJarPath(prefix, suffix, uriStringInput);
        assertNotNull(encodedJarPath);
        System.out.println("encodedJarPath = " + encodedJarPath);
        assertEquals("C:\\Prog_ram Files\\..\\esa-snap\\s{na}p\\m^odules\\eu-esa-sn`ap-esa-snappy.jar", encodedJarPath);
    }
}
