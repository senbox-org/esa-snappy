package org.esa.snap.snappy;

import java.nio.file.Files;
import java.nio.file.Path;

public class Configurator {

    private Path pythonExecutable;
    private Path pythonModuleInstallDir;

    public void setPythonExecutable(Path pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }

    public void setPythonModuleInstallDir(Path pythonModuleInstallDir) {
        this.pythonModuleInstallDir = pythonModuleInstallDir;
    }

    public ConfigurationReport doConfig() {
        ConfigurationReport report = new ConfigurationReport();
        if (pythonExecutable == null) {
            report.setException(new IllegalArgumentException("Python interpreter executable must be given"));
        }

        if (!Files.exists(pythonExecutable)) {
            report.setException(new IllegalArgumentException("Python interpreter executable not found: " + pythonExecutable));
        }

        try {
            Path snappyDir = PyBridge.installPythonModule(pythonExecutable, pythonModuleInstallDir, true);
            report.setEsaSnappyDir(snappyDir);
            report.setPythonExe(pythonExecutable);
            report.setSuccessful(true);
        } catch (Throwable t) {
            report.setException(new Exception("Python configuration error", t));
        }

        return report;
    }
}
