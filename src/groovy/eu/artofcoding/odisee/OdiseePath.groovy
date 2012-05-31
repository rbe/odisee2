/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee

import eu.artofcoding.odisee.helper.OdiseeConstant

/**
 * Odisee's paths, built from environment variables.
 */
@Singleton
class OdiseePath {

    /**
    /**
     * Debug?
     */
    static final Boolean ODISEE_DEBUG = Boolean.valueOf(System.getenv('ODISEE_DEBUG') ?: 'false')

     * Base directory for Odisee.
     */
    static final File ODISEE_HOME

    /**
     * Variable data directory for Odisee.
     */
    static final File ODISEE_VAR

    /**
     * Template directory.
     */
    static final File TEMPLATE_DIR

    /**
     * Where to put produced document?
     */
    static final File DOCUMENT_DIR

    static {
        String odiseeHome = System.getenv(OdiseeConstant.S_ODISEE_HOME)
        if (!odiseeHome) {
            throw new OdiseeException('Please set ODISEE_HOME')
        }
        // Setup path constants
        ODISEE_HOME = new File(System.getenv(OdiseeConstant.S_ODISEE_HOME)).absoluteFile
        if (System.getenv(OdiseeConstant.S_ODISEE_TMP)) {
            ODISEE_VAR = new File(System.getenv(OdiseeConstant.S_ODISEE_TMP)).absoluteFile
        } else {
            ODISEE_VAR = new File(ODISEE_HOME, OdiseeConstant.S_VAR_TMP_RAMDISK).absoluteFile
        }
        ODISEE_VAR.mkdirs()
        TEMPLATE_DIR = new File(ODISEE_VAR, OdiseeConstant.S_TEMPLATE)
        TEMPLATE_DIR.mkdir()
        DOCUMENT_DIR = new File(ODISEE_VAR, OdiseeConstant.S_DOCUMENT)
        DOCUMENT_DIR.mkdir()
        // Dump environment to stdout
        println 'Odisee environment variables:'
        println "  ODISEE_HOME =${ODISEE_HOME}"
        println "  ODISEE_VAR  =${ODISEE_VAR}"
        println "  TEMPLATE_DIR=${TEMPLATE_DIR}"
        println "  DOCUMENT_DIR=${DOCUMENT_DIR}"
    }

}
