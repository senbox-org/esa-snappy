package eu.esa.snap.snappy;

import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                } catch (RuntimeException e) {
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


    /**
     * todo
     *
     * @param pathToPythonExec
     * @return
     * @throws RuntimeException
     */
    static Path getPythonSitePackagesPath(String pathToPythonExec) throws RuntimeException {
        // todo: in case of error pass appropriate message(s) to the user.
        if (pathToPythonExec != null) {

            // obviously this is the most recommended way to detect the package installation dir for a given Python
            // see: https://discuss.python.org/t/understanding-site-packages-directories/12959
            //      https://ffy00.github.io/blog/02-python-debian-and-the-install-locations/
            final String[] commands = {
                    pathToPythonExec,
                    "-c",
                    "\"import sysconfig; print(sysconfig.get_path('platlib'))\""
            };

            try {
                Process process = Runtime.getRuntime().exec(commands);
                process.waitFor();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        StandardCharsets.UTF_8));

                String line;
                List lines = new ArrayList();

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

}
