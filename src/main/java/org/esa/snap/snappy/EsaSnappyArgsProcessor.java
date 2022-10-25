package org.esa.snap.snappy;

import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;

import java.nio.file.Path;
import java.nio.file.Paths;

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
        if (args.length >= 1) {
            configurator.setPythonExecutable(Paths.get(args[0]));
        }
        if (args.length >= 2) {
            configurator.setPythonModuleInstallDir(Paths.get(args[1]));
        }

        ConfigurationReport report = configurator.doConfig();

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

}
