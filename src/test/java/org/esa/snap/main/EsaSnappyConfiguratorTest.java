package org.esa.snap.main;


import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.TreeDeleter;
import org.esa.snap.snappy.PyBridge;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;


public class EsaSnappyConfiguratorTest {

    Path snapPythonPath;
    Path esaSnappyPath;
    Path snappyPropertiesFilePath;
    Path snappyCustomPath;

    @Before
    public void setUp() {
        snappyPropertiesFilePath = null;
        esaSnappyPath = null;
        snappyCustomPath = null;
        final Path snapApplicationPath = SystemUtils.getApplicationDataDir(true).toPath();
        snapPythonPath = snapApplicationPath.resolve(PyBridge.SNAP_PYTHON_DIRNAME);
    }

    // todo: The tests work, but find a better setup which does not overwrite/destroy an existing config
    //  in .snap/snap-python. Ignore for the moment.

    @Test
    @Ignore
    public void testConfigureWithDefaultSnappyDir() throws Exception {
        try {
            final String pythonExec =  System.getProperty("os.name").startsWith("Windows") ?  "python.exe" : "python";
            final String pythonExecPath = System.getenv("PYTHONHOME") + File.separator + pythonExec;
            try {
                Runtime.getRuntime().exec(pythonExecPath + " --version");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("No Python executable found, test is ignored. " +
                        "Make sure Python is installed and PYTHONHOME environment variable is set properly.");
                return;
            }

            EsaSnappyConfigurator.main(pythonExecPath);
            assertTrue(Files.isDirectory(snapPythonPath));
            snappyPropertiesFilePath = Paths.get(snapPythonPath + File.separator + PyBridge.SNAPPY_PROPERTIES_NAME);
            assertTrue(Files.isRegularFile(snappyPropertiesFilePath));
            esaSnappyPath = Paths.get(snapPythonPath + File.separator + PyBridge.SNAPPY_NAME);
            assertTrue(Files.isDirectory(esaSnappyPath));
        } finally {
            if (snappyPropertiesFilePath != null && Files.isRegularFile(snappyPropertiesFilePath)) {
                Files.deleteIfExists(snappyPropertiesFilePath);
            }
            if (esaSnappyPath != null && Files.isDirectory(esaSnappyPath)) {
                TreeDeleter.deleteDir(esaSnappyPath);
            }
        }
    }

    @Test
    @Ignore
    public void testConfigureWithCustomSnappyDir() throws Exception {
        try {
            final String pythonExec =  System.getProperty("os.name").startsWith("Windows") ?  "python.exe" : "python";
            final String pythonExecPath = System.getenv("PYTHONHOME") + File.separator + pythonExec;
            try {
                Runtime.getRuntime().exec(pythonExecPath + " --version");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("No Python executable found, test is ignored. " +
                        "Make sure Python is installed and PYTHONHOME environment variable is set properly.");
                return;
            }

            final String snappyCustomDirName = System.getProperty("user.home") + File.separator + "snap-python-TEST";
            snappyCustomPath = Paths.get(snappyCustomDirName);
            if (!Files.isDirectory(snappyCustomPath)) {
                final File filePath = new File(snappyCustomDirName);
                filePath.mkdir();
            }

            EsaSnappyConfigurator.main(pythonExecPath, snappyCustomDirName);
            assertTrue(Files.isDirectory(snappyCustomPath));
            snappyPropertiesFilePath = Paths.get(snapPythonPath + File.separator + PyBridge.SNAPPY_PROPERTIES_NAME);
            assertTrue(Files.isRegularFile(snappyPropertiesFilePath));
            assertTrue(Files.isDirectory(Paths.get(snappyCustomPath + File.separator + PyBridge.SNAPPY_NAME)));
        } finally {
            if (snappyPropertiesFilePath != null && Files.isRegularFile(snappyPropertiesFilePath)) {
                Files.deleteIfExists(snappyPropertiesFilePath);
            }
            if (snappyCustomPath != null && Files.isDirectory(snappyCustomPath)) {
                TreeDeleter.deleteDir(snappyCustomPath);
            }
        }
    }

}
