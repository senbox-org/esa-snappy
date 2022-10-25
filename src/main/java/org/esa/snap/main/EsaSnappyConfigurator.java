/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.snap.main;

import org.esa.snap.snappy.ConfigurationReport;
import org.esa.snap.snappy.Configurator;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *
 */
public class EsaSnappyConfigurator {

    public static void main(String... args) {
        try {
            applyConfiguration(args);
        } catch (Throwable e) {
            String message;
            if (e.getMessage() != null) {
                message = e.getMessage();
            } else {
                message = e.getClass().getName();
            }
            System.err.println("\nError: " + message);
            System.exit(1);
        }
    }

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
                exception.printStackTrace();
            }
        }
        System.out.flush();

    }

}
