package eu.esa.snap.snappy;


import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.TreeDeleter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class EsaSnappyArgsProcessorTest {

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
    public void testGetPythonModuleInstallDir() {

        final String expectedInstallDir = SystemUtils.getUserHomeDir().getAbsolutePath() + File.separator +
                "python310" + File.separator + "Lib" + File.separator + "site-packages";

        // Windows
        String pythonExecPath = SystemUtils.getUserHomeDir().getAbsolutePath() + File.separator + "python310" +
                File.separator + "python.exe";
        assertEquals(expectedInstallDir, EsaSnappyArgsProcessor.getPythonModuleInstallDir(pythonExecPath).toString());

        // Linux/Mac
        pythonExecPath = SystemUtils.getUserHomeDir().getAbsolutePath() + File.separator + "python310" +
                File.separator + "bin" + File.separator + "python";
        assertEquals(expectedInstallDir, EsaSnappyArgsProcessor.getPythonModuleInstallDir(pythonExecPath).toString());
    }

    @Test
    @Ignore
    public void testGetPythonSitePackagesPath() throws Exception {

        // todo: still fails
        final String pydirDummyWindows = getClass().getResource("pydir_dummy_windows").getPath() ;
        final String pyExecDummyWindows = pydirDummyWindows + File.separator + "python.exe";
        System.out.println();

        final Path actualSitePackagesPath = EsaSnappyArgsProcessor.getPythonSitePackagesPath(pyExecDummyWindows);
        assertNotNull(actualSitePackagesPath);

        final String expectedSitePackagesPath =
                pydirDummyWindows + File.separator + "Lib" + File.separator + "site-packages";
        assertEquals(expectedSitePackagesPath, actualSitePackagesPath.toString());
    }

}
