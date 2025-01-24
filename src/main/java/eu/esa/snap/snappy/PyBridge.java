/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package eu.esa.snap.snappy;

import org.esa.snap.core.util.Debug;
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.TreeCopier;
import org.esa.snap.core.util.io.TreeDeleter;
import org.esa.snap.runtime.Config;
import org.jpy.PyLib;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static eu.esa.snap.snappy.SnappyConstants.*;
import static org.esa.snap.core.util.SystemUtils.*;

/**
 * This class is used to establish the bridge between Java and Python.
 * It unpacks the Python module 'esa_' to a user configurable location
 * and executes the script 'snappy/snappyutil.py' with appropriate parameters.
 * <p>
 * 'snappyutil.py' again configures 'jpy' by selecting and unpacking appropriate
 * jpy tools and binaries found as 'jpy.&lt;platform&gt;-&lt;python-version&gt;.zip' in the 'lib' resources folder
 * of this Java module.
 * 'snappyutil.py' will then call 'jpyutil.py' to write the Java- and Python-side configuration files
 * 'jpyutil.properties' and 'jpyconfig.py'.
 * <p>
 * Then, 'jpyutil.properties' will be used by jpy's {@code PyLib} class to identify its correct binaries.
 * {@code PyLib} is finally used to start an embedded Python interpreter using the shared Python library that belongs
 * to the Python interpreter that was used to execute 'snappyutil.py'.
 * <p>
 * The following system properties can be used to configure this class:
 * <p>
 * <ol>
 * <li>{@code snap.pythonModuleDir}: The directory in which the Python module 'snappy' will be installed. The default value is {@code "~/modules/snap-python"}.</li>
 * <li>{@code snap.pythonExecutable}: The Python executable to be used with SNAP. The default value is {@code "python"}.</li>
 * <li>{@code snap.forcePythonConfig}: Forces reconfiguration of the bridge for each SNAP run. The default value is {@code "true"}</li>
 * <li>{@code snap.pythonExtraPaths}: Extra paths to be searched for SNAP Python extensions such as operators</li>
 * </ol>
 *
 * @author Norman Fomferra
 */
public class PyBridge {
    public static final Path PYTHON_CONFIG_DIR;
    private static final Path MODULE_CODE_BASE_PATH;

    private static boolean established;

    static {
        MODULE_CODE_BASE_PATH = findModuleCodeBasePath();
        PYTHON_CONFIG_DIR = SystemUtils.getApplicationDataDir(true).toPath().resolve(SNAP_PYTHON_DIRNAME);
    }

    /**
     * Establishes the SNAP-Python bridge.
     */
    public synchronized static void establish() throws IOException {
        if (established) {
            return;
        }

        Path snappyPath = installPythonModule(null, null, null);

        synchronized (PyLib.class) {
            if (!established) {
                startPython(snappyPath.getParent());
                established = true;
            }
        }
    }

    /**
     * Installs the SNAP-Python interface.
     *
     * @param pythonExecutable  The Python executable.
     * @param snappyParentDir   The directory into which the 'esa_snappy' Python module will be installed and configured.
     * @param forcePythonConfig If {@code true}, any existing installation / configuration will be overwritten.
     * @return The path to the configured 'snappy' Python module.
     * @throws IOException if something went wrong during file access
     */
    public synchronized static Path installPythonModule(Path pythonExecutable,
                                                        Path snappyParentDir,
                                                        Boolean forcePythonConfig) throws IOException {

        loadPythonConfig();

        if (pythonExecutable == null) {
            pythonExecutable = getPythonExecutable();
        }

        if (snappyParentDir == null) {
            snappyParentDir = getSnappyParentDir();
        }

        if (forcePythonConfig == null) {
            forcePythonConfig = isForceGeneratingNewPythonConfig();
        }

        Path snappyPath = snappyParentDir.resolve(SNAPPY_NAME);
        if (forcePythonConfig || !Files.isDirectory(snappyPath)) {
//            unpackPythonModuleDir(snappyPath);
            storePythonConfig(pythonExecutable, snappyParentDir);
        }

        Path jpyConfigFile = snappyPath.resolve(JPY_JAVA_API_CONFIG_FILENAME);
        if (forcePythonConfig || !Files.exists(jpyConfigFile)) {
            // Configure jpy Python-side
            configureJpy(pythonExecutable, snappyPath);
        }
        if (!Files.exists(jpyConfigFile)) {
            throw new IOException(String.format("SNAP-Python configuration incomplete.\n" +
                            "Missing file '%s'.\n" +
                            "Please check the log file '%s'.",
                    jpyConfigFile,
                    snappyPath.resolve(SNAPPYUTIL_LOG_FILENAME)));
        }

        // Configure jpy Java-side
        System.setProperty(JPY_CONFIG_PROPERTY, jpyConfigFile.toString());
        if (Debug.isEnabled() && System.getProperty(JPY_DEBUG_PROPERTY) == null) {
            System.setProperty(JPY_DEBUG_PROPERTY, "true");
        }

        // Configure Snapista:
//        configureSnapista(pythonExecutable, snappyPath);

        return snappyPath;
    }

    /**
     * Extends Python's system path (it's global {@code sys.path} variable) by the given path, if not already present.
     *
     * @param path The new module path.
     */
    public static void extendSysPath(String path) {
        if (path != null) {
            String code = String.format("" +
                            "import sys;\n" +
                            "p = '%s';\n" +
                            "if not p in sys.path: sys.path.append(p)",
                    path.replace("\\", "\\\\"));
            PyLib.execScript(code);
        }
    }

    // package local for testing!
    static String encodeJarPath(String prefix, String suffix, String input) {
        if (input.endsWith(suffix)) {
            input = input.substring(prefix.length(), input.length() - suffix.length());
        } else {
            int pos = input.indexOf(suffix);
            if (pos > 0) {
                input = input.substring(prefix.length(), pos);
            } else {
                input = input.substring(prefix.length());
            }
        }
        String encodedString;
        try {
            // We must decode the inner URI string first
            input = URLDecoder.decode(input, StandardCharsets.UTF_8);

            // Seems that this does the job for us and i.e. deals with Windows blank chars.
            // see https://stackoverflow.com/questions/4737841/urlencoder-not-able-to-translate-space-character
            encodedString = URI.create(new URI(null, null, input, null).toASCIIString()).toString();
        } catch (URISyntaxException e) {
            LOG.severe("Cannot encode '" + input + "'.");
            throw new RuntimeException(e);
        }

        return String.valueOf(Paths.get(URI.create(encodedString)));
    }

    private static void configureJpy(Path pythonExecutable, Path snappyDir) throws IOException {
        LOG.info("Configuring SNAP-Python interface...");

        // "java.home" is always present
        List<String> command = new ArrayList<>();
        command.add(pythonExecutable.toString());
        command.add(Paths.get(".", SNAPPYUTIL_PY_FILENAME).toString());
        command.add("--snap_home");
        command.add(SystemUtils.getApplicationHomeDir().getPath());
        command.add("--java_module");
        command.add(stripJarScheme(MODULE_CODE_BASE_PATH));
        command.add("--force");
        command.add("--log_file");
        command.add(Paths.get(".", SNAPPYUTIL_LOG_FILENAME).toString());
        if (Debug.isEnabled()) {
            command.add("--log_level");
            command.add("DEBUG");
        }

        String defaultJvmHeapSpace = getDefaultJvmHeapSpace();
        if (defaultJvmHeapSpace != null) {
            command.add("--jvm_max_mem");
            command.add(defaultJvmHeapSpace);
        }

        // "java.home" should always be present
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            command.add("--java_home");
            command.add(javaHome);
        }
        // "os.arch" should always be present
        String osArch = System.getProperty("os.arch");
        if (osArch != null) {
            // Note that we actually need the Java VM's architecture, and not the one of the host OS.
            // But there seems no way to retrieve it using Java JDK 1.8, so we stick to "os.arch".
            command.add("--req_arch");
            command.add(osArch);
        }
        String commandLine = toCommandLine(command);
        LOG.info(String.format("Executing command: [%s]\n", commandLine));
        try {
            Process process = new ProcessBuilder().command(command).directory(snappyDir.toFile()).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException(
                        String.format("Python configuration failed.\n" +
                                        "Command [%s]\nfailed with return code %s.\n" +
                                        "Please check the log file '%s'.",
                                commandLine, exitCode, snappyDir.resolve(SNAPPYUTIL_LOG_FILENAME)));
            }
        } catch (InterruptedException e) {
            throw new IOException(
                    String.format("Python configuration failed.\n" +
                                    "Command [%s]\nfailed with exception %s.\n" +
                                    "Please check the log file '%s'.",
                            commandLine, e.getMessage(), snappyDir.resolve(SNAPPYUTIL_LOG_FILENAME)), e);
        }
    }

    private static void configureSnapista(Path pythonExecutable, Path snappyDir) throws IOException {
        LOG.info("Configuring Snapista...");
        // todo: further check and investigate:
        // - make sure that pip is installed and found under all setups in order to run command below
        // - check this also for virtual envs
        // - try to output log messages not in SNAP messages.log, but on snappy_config console!
        // - Python psutils package should not be installed (not needed and requires Python >= 3.9

        List<String> command = new ArrayList<>();
        command.add(pythonExecutable.toString());
        command.add("-m");
        command.add("pip");
        command.add("install");
        Collections.addAll(command, SNAPISTA_REQUIRED_PACKAGES);

        String commandLine = toCommandLine(command);
        LOG.info(String.format("Executing command: [%s]\n", commandLine));
        try {
            Process process = new ProcessBuilder().command(command).directory(snappyDir.toFile()).start();
            int exitCode = process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                LOG.info(line);
            }

            reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            while ((line = reader.readLine()) != null) {
                LOG.severe(line);
            }

            LOG.info("Java Runtime.getRuntime(): exitValue = " + process.exitValue());
            reader.close();

            if (exitCode != 0) {
                System.out.println("Configuration of Snapista failed (IOException)!");
                throw new IOException(
                        String.format("Installation of Snapista Python packages with pip failed.\n" +
                                        "Command [%s]\nfailed with return code %s.\n" +
                                        "Please check your Python installation.",
                                commandLine, exitCode));
            } else {
                // todo: try to fix pip?
            }
        } catch (InterruptedException e) {
            System.out.println("Configuration of Snapista failed (InterruptedException)!");
            throw new IOException(
                    String.format("Python configuration failed.\n" +
                                    "Command [%s]\nfailed with exception %s.\n" +
                                    "Please check the log file '%s'.",
                            commandLine, e.getMessage(), snappyDir.resolve(SNAPPYUTIL_LOG_FILENAME)), e);
        }
    }

    private static String getDefaultJvmHeapSpace() {
        long totalMemory = getTotalPhysicalMemory();
        if (totalMemory > 0) {
            long memory = (long) (totalMemory * 0.7);
            long heapSpace = memory / (1024 * 1024 * 1024);
            return heapSpace + "G";
        } else {
            return null;
        }

    }

    private static long getTotalPhysicalMemory() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunMXBean = (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
            return sunMXBean.getTotalPhysicalMemorySize();
        }
        return -1L;
    }

    private static String toCommandLine(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (String arg : command) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(arg.contains(" ") ? String.format("\"%s\"", arg) : arg);
        }
        return sb.toString();
    }

    private static Path getResourcePath(String resource) {
        return MODULE_CODE_BASE_PATH.resolve(resource);
    }

    private static Path findModuleCodeBasePath() {
        return ResourceInstaller.findModuleCodeBasePath(PyBridge.class);
    }

    private static String stripJarScheme(Path path) {
        String prefix = "jar:";
        String suffix = "!/";
        String uriString = path.toUri().toString();

        if (uriString.startsWith(prefix)) {
//            begin test
//            run once for default Windows SNAP installation dir to make sure this works when used
//            with snappy-conf
//            String uriStringTest =
//                    "jar:file:///C:/Program Files/esa-snap/snap/modules/eu-esa-snap-esa-snappy.jar!/";
//            LOG.fine(String.format("TEST Jar string going into encodeJarTestPath: [%s]\n", uriStringTest));
//            final String encodeJarTestPath = encodeJarPath(prefix, suffix, uriStringTest);
//            LOG.fine(String.format("TEST Jar string coming from  encodeJarTestPath: [%s]\n", encodeJarTestPath));
//            end test

            LOG.fine(String.format("Jar string going into encodeJarTestPath: [%s]\n", uriString));
            final String encodedJarPath = encodeJarPath(prefix, suffix, uriString);
            LOG.fine(String.format("Jar string coming from  encodeJarTestPath: [%s]\n", encodedJarPath));
            return encodedJarPath;
        }

        return String.valueOf(path);
    }

    private static void unpackPythonModuleDir(Path pythonModuleDir) throws IOException {
        Files.createDirectories(pythonModuleDir);
        TreeCopier.copy(getResourcePath(SNAPPY_NAME), pythonModuleDir);
        LOG.info(String.format("SNAP-Python module '%s' located at %s", SNAPPY_NAME, pythonModuleDir));
    }

    private static boolean isForceGeneratingNewPythonConfig() {
        return Config.instance().preferences().getBoolean(FORCE_PYTHON_CONFIG_PROPERTY, false);
    }

    private static Path getPythonExecutable() {
        return Paths.get(Config.instance().preferences().get(PYTHON_EXECUTABLE_PROPERTY, "python"));
    }

    private static Path getSnappyParentDir() {
        Path pythonModuleInstallDir;
        String pythonModuleDirStr = Config.instance().preferences().get(PYTHON_MODULE_DIR_PROPERTY, null);
        if (pythonModuleDirStr != null) {
            pythonModuleInstallDir = Paths.get(pythonModuleDirStr);
        } else {
            pythonModuleInstallDir = PYTHON_CONFIG_DIR;
        }
        return pythonModuleInstallDir.toAbsolutePath().normalize();
    }


    private static void startPython(Path pythonModuleInstallDir) {
        //PyLib.Diag.setFlags(PyLib.Diag.F_ALL);
        String pythonVersion = PyLib.getPythonVersion();
        LOG.info("Starting Python " + pythonVersion);
        if (!PyLib.isPythonRunning()) {
            PyLib.startPython(pythonModuleInstallDir.toString());
        } else {
            extendSysPath(pythonModuleInstallDir.toString());
        }
    }


    private static void loadPythonConfig() {
        if (Files.isDirectory(PYTHON_CONFIG_DIR)) {
            Path pythonConfigFile = PYTHON_CONFIG_DIR.resolve(SNAPPY_PROPERTIES_NAME);
            if (Files.isRegularFile(pythonConfigFile)) {
                Properties properties = new Properties();
                try {
                    try (BufferedReader bufferedReader = Files.newBufferedReader(pythonConfigFile)) {
                        properties.load(bufferedReader);
                    }
                    Set<String> keys = properties.stringPropertyNames();
                    for (String key : keys) {
                        String value = properties.getProperty(key);
                        if (System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                        Config.instance().preferences().put(key, value);
                    }
                    LOG.info(String.format("SNAP-Python configuration loaded from '%s'", pythonConfigFile));
                } catch (IOException e) {
                    LOG.warning(String.format("Failed to load SNAP-Python configuration from '%s'", pythonConfigFile));
                }
            }
        }
    }

    private static void storePythonConfig(Path pythonExecutable,
                                          Path pythonModuleInstallDir) {

        Path pythonConfigFile = PYTHON_CONFIG_DIR.resolve(SNAPPY_PROPERTIES_NAME);
        try {
            if (!Files.exists(pythonConfigFile.getParent())) {
                Files.createDirectories(pythonConfigFile.getParent());
            }
            Properties properties = new Properties();
            properties.setProperty(PYTHON_EXECUTABLE_PROPERTY, pythonExecutable.toString());
            properties.setProperty(PYTHON_MODULE_DIR_PROPERTY, pythonModuleInstallDir.toString());
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(pythonConfigFile)) {
                properties.store(bufferedWriter, "Created by " + PyBridge.class.getName());
            }
            LOG.info(String.format("SNAP-Python configuration written to '%s'", pythonConfigFile));
        } catch (IOException e) {
            LOG.warning(String.format("Failed to store SNAP-Python configuration to '%s'", pythonConfigFile));
        }
    }
}
