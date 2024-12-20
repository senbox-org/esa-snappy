package eu.esa.snap.main;


import eu.esa.snap.snappy.SnappyConstants;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.TreeDeleter;
import eu.esa.snap.snappy.PyBridge;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;


public class EsaSnappyConfiguratorTest {

    // TODO: currently, tests are ignored by default. Try to configure something more general.

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
        snapPythonPath = snapApplicationPath.resolve(SnappyConstants.SNAP_PYTHON_DIRNAME);
    }

    @Test
    @Ignore
    public void testConfigureSnapPythonWithCustomSnappyDir() throws Exception {
        deleteOldConfig(snappyCustomPath);

        final String pythonExec = System.getProperty("os.name").startsWith("Windows") ? "python.exe" : "python";
        final String pythonExecPath = getPythonExecPath(pythonExec);

        final String snappyCustomDirName = System.getProperty("java.io.tmpdir") + File.separator + "snap-python-TEST";
        snappyCustomPath = Paths.get(snappyCustomDirName);
        if (!Files.isDirectory(snappyCustomPath)) {
            final File filePath = new File(snappyCustomDirName);
            filePath.mkdir();
        }

//        SnapArgsProcessor.processPython(new String[]{pythonExecPath, snappyCustomDirName});
        EsaSnappyConfigurator.main(pythonExecPath, snappyCustomDirName);
        assertTrue(Files.isDirectory(snappyCustomPath));
        snappyPropertiesFilePath = Paths.get(snapPythonPath + File.separator + SnappyConstants.SNAPPY_PROPERTIES_NAME);
        assertTrue(Files.isRegularFile(snappyPropertiesFilePath));
        assertTrue(Files.isDirectory(Paths.get(snappyCustomPath + File.separator + SnappyConstants.SNAPPY_NAME)));
    }

    @Test
    @Ignore
    public void testConfigureSnapPythonWithDefaultSnappyDir() throws Exception {
        deleteOldConfig(snapPythonPath);

        final String pythonExec = System.getProperty("os.name").startsWith("Windows") ? "python.exe" : "python";
        final String pythonExecPath = getPythonExecPath(pythonExec);

//        SnapArgsProcessor.processPython(new String[]{pythonExecPath});
        EsaSnappyConfigurator.main(pythonExecPath);
        assertTrue(Files.isDirectory(snapPythonPath));
        snappyPropertiesFilePath = Paths.get(snapPythonPath + File.separator + SnappyConstants.SNAPPY_PROPERTIES_NAME);
        assertTrue(Files.isRegularFile(snappyPropertiesFilePath));
        esaSnappyPath = Paths.get(snapPythonPath + File.separator + SnappyConstants.SNAPPY_NAME);
        assertTrue(Files.isDirectory(esaSnappyPath));
    }

    private static String getPythonExecPath(String pythonExec) {
        final String pythonExecPath = System.getenv("PYTHONHOME") + File.separator + pythonExec;
        try {
            Runtime.getRuntime().exec(pythonExecPath + " --version");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No Python executable found, test is ignored. " +
                    "Make sure Python is installed and PYTHONHOME environment variable is set properly.");
            return null;
        }
        return pythonExecPath;
    }

    private void deleteOldConfig(Path snapPythonPath) throws IOException {
        if (snapPythonPath != null && Files.isDirectory(snapPythonPath)) {
            TreeDeleter.deleteDir(snapPythonPath);
        }
    }

}
