/*
 * Odisee(R)
 *
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 23.11.14 16:03
 */

package eu.artofcoding.odisee.server

import java.nio.file.Path
import java.nio.file.Paths

public class JvmHelper {

    /**
     * Name of operating system.
     */
    public static final String OS_NAME = System.getProperty('os.name')

    public static Path findJar(String jar) {
        String[] classpath = System.getProperty(OdiseeConstant.JAVA_CLASS_PATH).split(File.pathSeparator);
        for (String p : classpath) {
            if (p.endsWith(jar)) {
                return Paths.get(p);
            }
        }
        return null;
    }

    public static Path findArchiveOfClass(Class<?> clazz) throws ClassNotFoundException {
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (null == path) {
            throw new ClassNotFoundException(String.format("Could not find class %s", clazz));
        }
        return Paths.get(path.substring(1));
    }

    public static Path findArchiveOfClass(String clazz) throws ClassNotFoundException {
        Class<?> klass = Class.forName(clazz);
        return findArchiveOfClass(klass);
    }

    public static ProcessIOStreamerThread pipe(final InputStream input, final PrintStream output, final String prefix) {
        ProcessIOStreamerThread t = new ProcessIOStreamerThread(input, output, prefix);
        t.start();
        return t;
    }

    public static String findTemporaryDirectory() {
        String odiseeTmp = System.getenv("ODISEE_TMP");
        if (null == odiseeTmp) {
            odiseeTmp = System.getProperty("java.io.tmpdir");
        }
        if (null == odiseeTmp) {
            for (String t : OdiseeConstant.TMP_DIR_NAMES) {
                if (null == odiseeTmp) {
                    odiseeTmp = System.getenv(t);
                } else {
                    break;
                }
            }
        }
        if (null == odiseeTmp) {
            throw new OdiseeServerException("Cannot find temporary directory, please set ODISEE_TMP");
        }
        String _odiseeTmp = odiseeTmp.replace('\\', '/');
        if (_odiseeTmp.endsWith("/")) {
            _odiseeTmp = _odiseeTmp.substring(0, _odiseeTmp.length() - 1);
        }
        return _odiseeTmp;
    }

}
