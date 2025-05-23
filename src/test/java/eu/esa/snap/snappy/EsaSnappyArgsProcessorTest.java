package eu.esa.snap.snappy;


import org.esa.snap.core.util.SystemUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;


public class EsaSnappyArgsProcessorTest {

    // TODO: currently, tests are ignored by default, as they need adaptation of local paths.
    //       Try to configure something more general.

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
    public void testGetPythonSitePackagesPath_main_install_dir() throws URISyntaxException {
        //test Windows with python.exe in main install dir:
        // before enabling the test, make sure this is an existing Python installation:
        String pyInstallWin = "D:" + File.separator + "olaf" + File.separator + "miniconda3_py310";
        String pyExec = pyInstallWin + File.separator + "python.exe";
        Path actualSitePackagesPath = EsaSnappyArgsProcessor.getPythonSitePackagesPath(pyExec);
        assertNotNull(actualSitePackagesPath);
        String expectedSitePackagesString = pyInstallWin  + "/Lib/site-packages/";
        Path expectedSitePackagesPath = Paths.get(expectedSitePackagesString);
        assertEquals(expectedSitePackagesPath, actualSitePackagesPath);


    }


    @Test
    @Ignore
    public void testGetPythonSitePackagesPath_virtualenv() throws URISyntaxException {
        //test Windows with python.exe in virtual env dir:
        // before enabling the test, make sure this is an existing Python installation:
        String  pyInstallWin = "D:" + File.separator + "olaf" + File.separator + "miniconda3_py38" +
                File.separator + "envs" + File.separator + "testenv_39";
        String  pyExec = pyInstallWin + File.separator + "python.exe";
        Path actualSitePackagesPath = EsaSnappyArgsProcessor.getPythonSitePackagesPath(pyExec);
        assertNotNull(actualSitePackagesPath);
        String  expectedSitePackagesString = pyInstallWin  + "/Lib/site-packages/";
        Path expectedSitePackagesPath = Paths.get(expectedSitePackagesString);
        assertEquals(expectedSitePackagesPath, actualSitePackagesPath);
    }

    @Test
    @Ignore
    public void testCheckIfPathExists() {
        final String existing = "D:" + File.separator + "olaf";
        final String notExisting = "D:" + File.separator + "olaf" + File.separator + "fakedir";
        final Path existingPath = Paths.get(existing);
        final File existingFile = existingPath.toFile();
        final Path notExistingPath = Paths.get(notExisting);
        final File notExistingFile = notExistingPath.toFile();
        assertTrue(existingFile.exists());
        assertFalse(notExistingFile.exists());

        final File existingSubdir = new File(existingPath.toFile().getAbsolutePath() + File.separator + "bc");
        final File notExistingSubdir = new File(existingPath.toFile().getAbsolutePath() + File.separator + "not_bc");
        assertTrue(existingSubdir.exists());
        assertFalse(notExistingSubdir.exists());
    }
}
