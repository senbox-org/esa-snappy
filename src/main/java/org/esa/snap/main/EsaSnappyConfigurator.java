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

import org.esa.snap.core.util.StopWatch;
import org.esa.snap.snappy.PyBridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *
 */
public class EsaSnappyConfigurator {

    public static void main(String... args) {
        try {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            run(args);
            stopWatch.stopAndTrace("ESA SNAPPY Configuration");
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

    public static void run(String[] args) throws Exception {
        processPython(args);
    }

    private static void processPython(String[] args) throws IOException {

        Path pythonExecutable = null;
        Path pythonModuleInstallDir = null;

        if (args.length >= 1) {
            pythonExecutable = Paths.get(args[0]);
        }
        if (args.length >= 2) {
            pythonModuleInstallDir = Paths.get(args[1]);
        }

        if (pythonExecutable == null) {
            throw new IOException("Python interpreter executable must be given");
        }

        if (!Files.exists(pythonExecutable)) {
            throw new IOException("Python interpreter executable not found: " + pythonExecutable);
        }

        try {
            System.out.flush();
            System.out.println("Configuring SNAP-Python interface...");
            Path snappyDir = PyBridge.installPythonModule(pythonExecutable, pythonModuleInstallDir, true);
            System.out.flush();
            System.out.printf("Done. The SNAP-Python interface is located in '%s'%n", snappyDir);
            System.out.printf("When using SNAP from Python, either do: sys.path.append('%s')%n", snappyDir.getParent().toString().replace("\\", "\\\\"));
            System.out.printf("or copy the '%s' module into your Python's 'site-packages' directory.%n", snappyDir.getFileName());
            System.out.flush();
        } catch (IOException e) {
            e.printStackTrace(System.out);
            System.out.flush();
            throw new IOException("Python configuration error: " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.out.flush();
            throw new IOException("Python configuration internal error: " + t.getMessage());
        }
    }
}
