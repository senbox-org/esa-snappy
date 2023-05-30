package eu.esa.snap.snappy;

import java.nio.file.Path;

public class ConfigurationReport {
    private Exception exception;
    private boolean successful;
    private Path snappyDir;
    private Path pythonExe;
    private String errorMessage;

    public void setSuccessful(boolean result) {
        this.successful = result;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setException(Exception exception) {
        this.exception = exception;
        errorMessage = exception.getMessage();
        successful = false;
    }

    public Exception getException() {
        return exception;
    }

    public void setEsaSnappyDir(Path snappyDir) {
        this.snappyDir = snappyDir;
    }

    public Path getSnappyDir() {
        return snappyDir;
    }

    public void setPythonExe(Path executable) {
        pythonExe = executable;
    }

    public Path getPythonExe() {
        return pythonExe;
    }
}
