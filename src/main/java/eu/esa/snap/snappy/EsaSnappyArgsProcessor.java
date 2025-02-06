package eu.esa.snap.snappy;

import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EsaSnappyArgsProcessor  implements ArgsProcessor {

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
//        if (args.length >= 1) {
//            configurator.setPythonExecutable(Paths.get(args[0]));
//        }
//        if (args.length >= 2) {
//            configurator.setPythonModuleInstallDir(Paths.get(args[1]));
//        }

        // use Python executble as only parameter, and install in Lib/site-packages of current Python
        // todo: check with ESA
        if (args.length != 1) {
            throw new IllegalArgumentException("EsaSnappyArgsProcessor: wrong number of arguments: " + args.length);
        } else {
            configurator.setPythonExecutable(Paths.get(args[0]));
            configurator.setPythonModuleInstallDir(getPythonModuleInstallDir(args[0]));
        }

        ConfigurationReport report = configurator.doConfig();

        // todo: check and improve this output, in particular in error case with empty stack trace!
        if(report.isSuccessful()) {
            System.out.println("Configuration finished successful!");
            Path snappyDir = report.getSnappyDir();
            System.out.printf("Done. The SNAP-Python interface is located in '%s'%n", snappyDir);
            System.out.printf("When using SNAP from Python, either do: sys.path.append('%s')%n", snappyDir.getParent().toString().replace("\\", "\\\\"));
            System.out.printf("or copy the '%s' module into your Python's 'site-packages' directory.%n", snappyDir.getFileName());
            System.out.printf("The executable of the Python environment is located at '%s'%n", report.getPythonExe());
        }else {
            System.out.println("Configuration failed!");
            System.out.printf("Error: %s%n", report.getErrorMessage());
            Exception exception = report.getException();
            if (exception != null) {
                System.out.println("Full stack trace:");
                exception.printStackTrace(System.err);
            }
        }
        System.out.flush();

    }

    public static Path getPythonModuleInstallDir(String pathToPythonExec) {
        if (pathToPythonExec != null) {
            if (pathToPythonExec.endsWith("python")) {
                // Linux/Macos
                // e.g. /home/Python310/bin/python
                return Paths.get(new File(pathToPythonExec).getParentFile().getParentFile().getAbsolutePath() +
                        File.separator + "Lib" + File.separator + "site-packages");
            } else if (pathToPythonExec.endsWith("python.exe")) {
                // Windows
                // e.g. C:\\User\\Python310\\python.exe
                return Paths.get(new File(pathToPythonExec).getParentFile().getAbsolutePath() +
                        File.separator + "Lib" + File.separator + "site-packages");
            } else {
                throw new IllegalArgumentException("Input does not seem to be a path to a Python executable");
            }
        }
        return null;
    }

    public static Path getPythonSitePackagesPath(String pathToPythonExec) throws IOException {
        // todo: write test. If ok, replace getPythonModuleInstallDir(..)
        if (pathToPythonExec != null) {

            Path pythonRootPath;

            final String osName = System.getProperty("os.name").toLowerCase();
            if ((osName.contains("linux") || osName.contains("mac")) && pathToPythonExec.endsWith("python")) {
                // e.g. /home/Python310/bin/python
                pythonRootPath = Paths.get(new File(pathToPythonExec).getParentFile().getParentFile().getAbsolutePath());
            } else if ((osName.contains("windows")) && pathToPythonExec.endsWith("python.exe")) {
                // e.g. C:\\User\\Python310\\python.exe
                pythonRootPath = Paths.get(new File(pathToPythonExec).getParentFile().getAbsolutePath());
            } else {
                throw new IllegalArgumentException("Input does not seem to be a path to a Python executable");
            }

            try {
                Files.walk(pythonRootPath).forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Optional<Path> foundPath;
            try (Stream<Path> paths2 = Files.walk(pythonRootPath)) {
                // todo: check for envs
                foundPath = paths2.filter(Files::isDirectory)
                        .filter(path -> {
                            final String s = path.getFileName().toString();
//                            System.out.println("s = " + s);
                            return s.equals("site-packages");
                        })
                        .findFirst();
            }

            if (foundPath.isPresent()) {
                System.out.printf("Python site packages dir: " + foundPath.get().toAbsolutePath());
                return foundPath.get();
            }

            return foundPath.isPresent() ? foundPath.get() : null;

        }
        return null;
    }

}
