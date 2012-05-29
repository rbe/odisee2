/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.ooo.server

import eu.artofcoding.odisee.helper.JvmHelper

/**
 * Created by IntelliJ IDEA.
 * User: rbe
 * Date: 09.03.12
 * Time: 18:49
 * To change this template use File | Settings | File Templates.
 */
class OOoInstallation {

    private static final String SOFFICE_PROGRAM_DIR = 'program'
    private static final String OS_MAC_OS = 'MacOS'
    private static final String SOFFICE_BIN = 'soffice.bin'

    /**
     * Recurse a directory and look for all files with a certain name.
     */

    private List look(String pat, File dir, List result = []) {
        dir.listFiles().each {
            if (it.isDirectory() /* JDK7 && !it.isSymbolicLink()*/) {
                look(pat, it, result)
            } else if (it.name ==~ pat) {
                result << it
            }
        }
        result
    }

    /**
     * Find OpenOffice installation.
     * @return List < File >  Path to installation (program/ folder).
     */
    List<File> findOOoInstallation() {
        List<File> result
        // Check environment variable OOO_HOME
        String OOO_HOME = System.getenv('OOO_HOME')
        if (OOO_HOME) {
            switch (OS_NAME) {
            // MacOS X: Don't use program link
            // http://www.openoffice.org/issues/show_bug.cgi?id=101203
                case { it ==~ JvmHelper.OS_DARWIN }:
                    File file = new File(OOO_HOME, OS_MAC_OS)
                    if (file.exists()) {
                        result = [file]
                    }
                    break
            // All other
                default:
                    File file = new File(OOO_HOME, SOFFICE_PROGRAM_DIR)
                    if (file.exists()) {
                        result = [file]
                    }
                    break
            }
        }
        if (result) {
            result
        } else {
            // Look for soffice executable depending on operating system
            switch (OS_NAME) {
            // Mac OS X: make an educated guess
                case { it ==~ OS_DARWIN }:
                    File guess = new File('/Applications/OpenOffice.org.app/Contents/MacOS/soffice')
                    if (guess.exists()) {
                        result = [guess]
                    } else {
                        result = File.listRoots().collectMany { root ->
                            ['Applications/Oracle Open Office.app', 'Applications/LibreOffice.app'].collect { folder ->
                                look(SOFFICE_BIN, new File(root, folder))
                            }
                        }
                    }
                    break
            // UNIX: look in /usr/lib and /opt folders
                case { it in [JvmHelper.OS_LINUX, JvmHelper.OS_SUNOS] || it ==~ JvmHelper.OS_BSD }:
                    result = ['/usr/lib', '/opt'].collectMany { folder ->
                        look(SOFFICE_BIN, new File('/', folder))
                    }
                    break
            // Windows: look in different drives, Program* folders
                case { it ==~ OS_WINDOWS }:
                    List drives = File.listRoots().findAll {
                        it.absolutePath ==~ /[CDE]:.*/
                    }
                    result = drives.collectMany { root ->
                        ['Programme', 'Program Files', 'Program Files (x86)'].collect { folder ->
                            look(SOFFICE_BIN, new File(root, folder))
                        }
                    }
                    break
            }
            //println "result=${result?.dump()}"
            result ? result.parentFile : null
        }
    }

}
