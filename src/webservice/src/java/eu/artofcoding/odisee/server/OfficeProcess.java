/*
 * Odisee(R)
 * odisee-server, odisee-xml-processor
 *
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com/
 * Copyright (C) 2011-2012 art of coding UG, http://www.art-of-coding.eu/
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 04.08.12 17:19
 */

package eu.artofcoding.odisee.server;

import com.sun.star.lib.util.NativeLibraryLoader;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class OfficeProcess {

    public String officeProgramPath = "/Applications/LibreOffice.app/Contents/MacOS";

    private static void pipe(final InputStream in, final PrintStream out, final String prefix) {
        new Thread("Pipe: " + prefix) {
            @Override
            public void run() {
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                try {
                    for (; ; ) {
                        String s = r.readLine();
                        if (s == null) {
                            break;
                        }
                        out.println(prefix + s);
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }.start();
    }

    private static List<String> getOptions() {
        List<String> options = new ArrayList<String>();
        options.add("--headless");
        options.add("--nologo");
        options.add("--nodefault");
        options.add("--norestore");
        options.add("--nolockcheck");
        return options;
    }

    public Process startOfficeProcess(InetSocketAddress socketAddress) throws OdiseeServerException {
        String sOffice = System.getProperty("os.name").startsWith("Windows") ? "soffice.exe" : "soffice";
        File officeExecutable;
        try {
            URL[] oooExecFolderURL = new URL[]{new File(officeProgramPath).toURI().toURL()};
            URLClassLoader loader = new URLClassLoader(oooExecFolderURL);
            officeExecutable = NativeLibraryLoader.getResource(loader, sOffice);
            if (officeExecutable == null) {
                throw new OdiseeServerException("No office executable found!");
            }
        } catch (MalformedURLException e) {
            throw new OdiseeServerException("Invalid path (" + officeProgramPath + ") to office executable", e);
        }
        File programDir = officeExecutable.getParentFile();
        // Make UNO url
        String unoURL = UNOHelper.makeUnoUrl(socketAddress);
        // Get temporary directory
        String odisee_tmp = findTemporaryDirectory();
        // Create call with arguments
        String[] oooCommand = makeCommandLineOptions(officeExecutable, unoURL, odisee_tmp);
        try {
            Process process = Runtime.getRuntime().exec(oooCommand, null, programDir);
            pipe(process.getInputStream(), System.out, "CO> ");
            pipe(process.getErrorStream(), System.err, "CE> ");
            return process;
        } catch (IOException e) {
            throw new OdiseeServerException("Cannot start office process", e);
        }
    }

    private String[] makeCommandLineOptions(File officeExecutable, String unoURL, String odisee_tmp) throws OdiseeServerException {
        List<String> options = new ArrayList<String>();
        options.add("-env:UserInstallation=\"file:///" + odisee_tmp + "/odisee_port" + UNOHelper.getPort(unoURL) + "\"".replaceAll("//", "/"));
        options.add("--accept=\"" + unoURL + "\"");
        options.addAll(getOptions());
        int arguments = options.size() + 1;
        String[] cmd = new String[arguments];
        String prefix = System.getProperty("os.name").startsWith("Windows") ? "" : "./soffice";
        cmd[0] = prefix + officeExecutable.getName();
        for (int i = 0; i < options.size(); i++) {
            cmd[i + 1] = (String) options.get(i);
        }
        return cmd;
    }

    private String findTemporaryDirectory() throws OdiseeServerException {
        String odisee_tmp = System.getenv("ODISEE_TMP");
        if (null == odisee_tmp) {
            odisee_tmp = System.getProperty("java.io.tmpdir");
        }
        if (null == odisee_tmp) {
            String[] checkTemp = new String[]{"TMP", "TMPDIR", "TEMP"};
            for (String t : checkTemp) {
                if (null == odisee_tmp) {
                    odisee_tmp = System.getenv("TMP");
                } else {
                    break;
                }
            }
        }
        if (null == odisee_tmp) {
            throw new OdiseeServerException("Cannot start process, please set ODISEE_TMP");
        }
        return odisee_tmp;
    }

    public static void main(String[] args) throws OdiseeServerException, InterruptedException {
        final OfficeProcess officeProcess = new OfficeProcess();
        final InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 2001);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    officeProcess.startOfficeProcess(socketAddress);
                } catch (OdiseeServerException e) {
                    throw new OdiseeServerRuntimeException(e);
                }
            }
        };
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        Thread.sleep(5 * 1000);
    }

}
