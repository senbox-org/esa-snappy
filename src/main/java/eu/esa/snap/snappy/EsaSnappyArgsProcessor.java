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
/**
 * EsaSnappy command-line arguments:
 * <ul>
 * <li>{@code --snappy <python-interpreter> [<snappy-python-module-dir>]}</li>
 * </ul>
 *
 * @author Norman Fomferra, Olaf Danne
 */
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

    /**
     * Initiates the SNAP-Python configuration with given arguments.
     *
     * @param args : 1) full path to Python executable (mandatory)
     *               2) Directory in which esa_snappy package has been installed from PyPi (optional)
     */
    public static void applyConfiguration(String[] args) {
        System.out.flush();
        System.out.println("Configuring ESA SNAP-Python interface...");
        System.out.flush();

        Configurator configurator = new Configurator();

        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("EsaSnappyArgsProcessor: wrong number of arguments: " + args.length);
        } else {
            // use Python executable as only parameter, and install in Lib/site-packages of current Python
            setPythonExecForConfig(args, configurator);
            if (args.length == 1) {
                try {
                    // determine default esa_snappy install dir. Should be in Lib/site-packages of current Python!
                    final Path sitePackagesPath = getPythonSitePackagesPath(args[0]);
                    if (sitePackagesPath != null) {
                        // We need to check if esa_snappy package has been installed from PyPi before:
                        final File esaSnappyPackageDir =
                                new File(sitePackagesPath.toFile().getAbsolutePath() + File.separator + "esa_snappy");
                        // todo: improve: check for mandatory content in esa_snappy dir
                        if (esaSnappyPackageDir.exists()) {
                            System.out.printf("Found esa_snappy installed in '%s'%n", sitePackagesPath);
                            System.out.println("Starting configuration...");
                            configurator.setPythonModuleInstallDir(sitePackagesPath);
                        } else {
                            throw new IllegalArgumentException("\nError: esa_snappy package '" +
                                    esaSnappyPackageDir.getAbsolutePath() +
                                    "' does not exist.\n Maybe it was not yet installed from PyPi?");
                        }
                    } else {
                        throw new RuntimeException("Configuration failed:  Python site-packages path could not be determined.");
                    }

                } catch (RuntimeException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                setPythonModuleInstallDirForConfig(args[1], configurator);
            }
        }

        ConfigurationReport report = configurator.doConfig();

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
                System.out.printf("Exception: %s%n", exception.getMessage());
                exception.printStackTrace(System.err);
            }
        }
        System.out.flush();

    }


    /**
     * For a given path to a Python executable, this method returns the corresponding site-packages path
     * of the Python installation.
     *
     * @param pathToPythonExec - full path to Python executable (as String)
     * @return site packages path
     * @throws RuntimeException -
     * @throws URISyntaxException -
     */
    static Path getPythonSitePackagesPath(String pathToPythonExec) throws RuntimeException, URISyntaxException {
        if (pathToPythonExec != null) {

            String[] commands;

            // obviously this is the most recommended way to detect the package installation dir for a given Python
            // see: https://discuss.python.org/t/understanding-site-packages-directories/12959
            //      https://ffy00.github.io/blog/02-python-debian-and-the-install-locations/

            // As 'python -c "..."' does not provide the expected output on Linux,
            // we introduce a small Python script as resource doing the job.
            // Unfortunately again, we cannot use this directly as file from a jar (URI is not hierarchical),
            // so we must read from an input stream and copy into a temp file (we need a Python file!):
            InputStream in = Objects.requireNonNull(EsaSnappyArgsProcessor.class.
                    getResourceAsStream("get_site_packages_dir.py"));
            File tempFile;
            try {
                tempFile = Files.createTempFile("", ".py").toFile();
                FileUtils.copyInputStreamToFile(in, tempFile);
            } catch (IOException e) {
                throw new RuntimeException("Cannot determine Python site-packages path: " + e.getMessage());
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
//                    System.out.printf(line);
                    // store output in a list of lines. Anyway, the result should be just one line.
                    lines.add(line);
                }

                reader.close();

                if (process.exitValue() != 0) {
                    throw new RuntimeException("External Python call terminated with an error.");
                }

                final String[] linesArr = lines.toArray(new String[0]);
                return Paths.get(linesArr[0]);

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Determination of Python site-packages path failed: " + e.getMessage());
            }
        }
        return null;
    }

    private static void setPythonModuleInstallDirForConfig(String installDir, Configurator configurator) {
        final Path installPath = Paths.get(installDir);
        if (installPath.toFile().exists()) {
            configurator.setPythonModuleInstallDir(installPath);

            final File esaSnappyInstallDir = new File(installDir + File.separator + "esa_snappy");
            // todo: improve: check for mandatory content in esa_snappy dir
            if (esaSnappyInstallDir.exists()) {
                System.out.printf("Found esa_snappy installed in '%s'%n", installDir);
                System.out.println("Starting configuration...");
                configurator.setPythonModuleInstallDir(installPath);
            } else {
                throw new IllegalArgumentException("\nError: esa_snappy package '" +
                        esaSnappyInstallDir.getAbsolutePath() +
                        "' does not exist.\n Maybe it was not yet installed from PyPi?");
            }

        } else {
            throw new IllegalArgumentException("Python module installation directory '" +
                    installDir + "' does not exist.");
        }
    }

    private static void setPythonExecForConfig(String[] args, Configurator configurator) {
        if (Paths.get(args[0]).toFile().exists()) {
            configurator.setPythonExecutable(Paths.get(args[0]));
        } else {
            throw new IllegalArgumentException("Python executable '" + args[0] + "' does not exist.");
        }
    }

}
