package eu.esa.snap.snappy;

import org.apache.commons.io.FileUtils;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EsaSnappyArgsProcessor implements ArgsProcessor {

    @Arg(longName = "snappy")
    @Description(shortDescription = "Configure the ESA SNAP Java-Python adapter 'esa_snappy': snap --nogui --nosplash --snappy <python-interpreter> [<snappy-python-module-dir>]")
    public String[] pythonArgs;

    @Override
    public void process(Env env) {
        if (pythonArgs != null) {
            applyConfiguration(pythonArgs);
        }

    }

    // can be private if not used by EsaSnappyConfigurator anymore
    public static void applyConfiguration(String[] args) {
        System.out.flush();
        System.out.println("Configuring ESA SNAP-Python interface...");
        System.out.flush();

        Configurator configurator = new Configurator();
        if (args.length == 1) {
            // only Python executable is provided. esa_snappy installation dir will be determined below.
            configurator.setPythonExecutable(Paths.get(args[0]));
        }
        if (args.length == 2) {
            configurator.setPythonExecutable(Paths.get(args[0]));
            // esa_snappy installation dir is given by the user
            configurator.setPythonModuleInstallDir(Paths.get(args[1]));
        }

        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("EsaSnappyArgsProcessor: wrong number of arguments: " + args.length);
        } else {
            // use Python executble as only parameter, and install in Lib/site-packages of current Python
            configurator.setPythonExecutable(Paths.get(args[0]));
            if (args.length == 1) {
                try {
                    // determine default esa_snappy install dir. Should be in Lib/site-packages of current Python!
                    configurator.setPythonModuleInstallDir(getPythonSitePackagesPath(args[0]));
                } catch (RuntimeException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                configurator.setPythonModuleInstallDir(Paths.get(args[1]));
            }
        }

        ConfigurationReport report = configurator.doConfig();

        // todo: check and improve this output, in particular in error case with empty stack trace!
        if (report.isSuccessful()) {
            System.out.println("Configuration finished successful!");
            Path snappyDir = report.getSnappyDir();
            System.out.printf("Done. The SNAP-Python interface is located in '%s'%n", snappyDir);
            System.out.printf("The executable of the Python environment is located at '%s'%n", report.getPythonExe());
        } else {
            System.out.println("Configuration failed!");
            if (report.getErrorMessage() != null && report.getErrorMessage().length() > 0) {
                System.out.printf("Error: %s%n", report.getErrorMessage());
            }
            Exception exception = report.getException();
            if (exception != null && exception.getMessage().length() > 0) {
                System.out.println("Full stack trace:");
                exception.printStackTrace(System.err);
            }
        }
        System.out.flush();

    }


    static Path getPythonSitePackagesPath(String pathToPythonExec) throws RuntimeException, URISyntaxException {
        // todo: in case of error pass appropriate message(s) to the user.
        if (pathToPythonExec != null) {

            String[] commands;

            // obviously this is the most recommended way to detect the package installation dir for a given Python
            // see: https://discuss.python.org/t/understanding-site-packages-directories/12959
            //      https://ffy00.github.io/blog/02-python-debian-and-the-install-locations/

            // We introduce a small Python script as resource doing this.
            // Unfortunately again, we cannot use this directly as file from a jar (URI is not hierarchical),
            // so we must read from an input stream and copy into a temp file (we need a Python file!):
            InputStream in = Objects.requireNonNull(EsaSnappyArgsProcessor.class.
                    getResourceAsStream("get_site_packages_dir.py"));
            File tempFile;
            try {
                tempFile = Files.createTempFile("", ".py").toFile();
                FileUtils.copyInputStreamToFile(in, tempFile);
            } catch (IOException e) {
                // todo
                throw new RuntimeException(e);
            }

            commands = new String[]{
                    pathToPythonExec,
                    "-u",
                    tempFile.getAbsolutePath(),
                    "print_site_packages_dir"
            };

            try {
                ProcessBuilder pb = new ProcessBuilder().command(commands);
                final Process process = pb.start();
                process.waitFor();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        StandardCharsets.UTF_8));

                String line;
                List<String> lines = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    System.out.printf(line);
                    // store output in a list of lines. Anyway, the result should be just one line.
                    lines.add(line);
                }

                reader.close();

                if (process.exitValue() != 0) {
                    throw new RuntimeException("External Python call terminated with an error.");
                }

                final String[] linesArr = (String[]) lines.toArray(new String[0]);
                return Paths.get(linesArr[0]);

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Determination of Python site-packages path failed: " + e.getMessage());
            }
        }
        return null;
    }

    static Path getPythonSitePackagesPath_test(String pathToPythonExec) throws RuntimeException, URISyntaxException {
        // todo: in case of error pass appropriate message(s) to the user.
        if (pathToPythonExec != null) {

            String[] commands;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                String pythonRootDir;
                Path sitePackagesPath;

                // keep it simple for Windows...
                final String pythonExeParentDir = new File(pathToPythonExec).getParentFile().getAbsolutePath();
                if (pythonExeParentDir.endsWith("Scripts")) {
                    // virtual env for Python from python.org:
                    // <pyInstallDir>\envs\<env_name>\Scripts\python.exe
                    // <pyInstallDir>\envs\<env_name>\Lib\site-packages
                    pythonRootDir = new File(pythonExeParentDir).getParentFile().getAbsolutePath();
                    sitePackagesPath = Paths.get(pythonRootDir + File.separator + "Lib" + File.separator + "site-packages");
                } else {
                    // main dir for Python from python.org, main dir or virtual env for Python from Anaconda/Miniconda:
                    // <pyInstallDir>\python.exe
                    // <pyInstallDir>\Lib\site-packages
                    // <pyInstallDir>\envs\<env_name>\python.exe
                    // <pyInstallDir>\envs\<env_name>\Lib\site-packages
                    pythonRootDir = new File(pathToPythonExec).getParentFile().getAbsolutePath();
                    sitePackagesPath = Paths.get(pythonRootDir + File.separator + "Lib" + File.separator + "site-packages");
                }

                return sitePackagesPath;
            } else {
                // obviously this is the most recommended way to detect the package installation dir for a given Python
                // see: https://discuss.python.org/t/understanding-site-packages-directories/12959
                //      https://ffy00.github.io/blog/02-python-debian-and-the-install-locations/

                // We introduce a small Python script as resource doing this.
                // Unfortunately again, we cannot use this directly as file from a jar (URI is not hierarchical),
                // so we must read from an input stream and copy into a temp file (we need a Python file!):
                InputStream in = Objects.requireNonNull(EsaSnappyArgsProcessor.class.
                        getResourceAsStream("get_site_packages_dir.py"));
                File tempFile;
                try {
                    tempFile = Files.createTempFile("", ".py").toFile();
                    FileUtils.copyInputStreamToFile(in, tempFile);
                } catch (IOException e) {
                    // todo
                    throw new RuntimeException(e);
                }

                commands = new String[]{
                        pathToPythonExec,
                        "-u",
                        tempFile.getAbsolutePath(),
                        "print_site_packages_dir"
                };

                try {
                    ProcessBuilder pb = new ProcessBuilder().command(commands);
                    final Process process = pb.start();
                    process.waitFor();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                            StandardCharsets.UTF_8));

                    String line;
                    List<String> lines = new ArrayList<>();

                    while ((line = reader.readLine()) != null) {
                        System.out.printf(line);
                        // store output in a list of lines. Anyway, the result should be just one line.
                        lines.add(line);
                    }

                    reader.close();

                    if (process.exitValue() != 0) {
                        throw new RuntimeException("External Python call terminated with an error.");
                    }

                    final String[] linesArr = (String[]) lines.toArray(new String[0]);
                    return Paths.get(linesArr[0]);

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Determination of Python site-packages path failed: " + e.getMessage());
                }
            }
        }
        return null;
    }

}
