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
     * Profile?
     */
    static final Boolean ODISEE_PROFILE = Boolean.valueOf(System.getenv('ODISEE_PROFILE') ?: 'false')

    /**
     * Debug?
     */
    static final Boolean ODISEE_DEBUG = Boolean.valueOf(System.getenv('ODISEE_DEBUG') ?: 'false')

    /**
     * Home: installation path of OpenOffice.
     */
    static final String OOO_HOME = System.getenv('OOO_HOME')

    /**
     * Base directory for Odisee.
     */
    static final File ODISEE_HOME

    /**
     * Deployment directory for Odisee, contains additional files.
     */
    static final File ODISEE_DEPLOY

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
        // Is Odisee home set?
        if (!System.getenv(OdiseeConstant.S_ODISEE_HOME)) {
            throw new OdiseeException('Please set ODISEE_HOME')
        }
        // Setup path constants
        // ODISEE_HOME
        ODISEE_HOME = new File(System.getenv(OdiseeConstant.S_ODISEE_HOME)).absoluteFile
        // ODISEE_DEPLOY
        if (System.getenv(OdiseeConstant.S_ODISEE_DEPLOY)) {
            ODISEE_DEPLOY = new File(System.getenv(OdiseeConstant.S_ODISEE_DEPLOY)).absoluteFile
        } else {
            ODISEE_DEPLOY = new File(ODISEE_HOME, 'var/deploy').absoluteFile
        }
        ODISEE_DEPLOY.mkdirs()
        // ODISEE_VAR
        if (System.getenv(OdiseeConstant.S_ODISEE_TMP)) {
            ODISEE_VAR = new File(System.getenv(OdiseeConstant.S_ODISEE_TMP)).absoluteFile
        } else {
            ODISEE_VAR = new File(ODISEE_HOME, OdiseeConstant.S_VAR_TMP_RAMDISK).absoluteFile
        }
        ODISEE_VAR.mkdirs()
        // Templates
        TEMPLATE_DIR = new File(ODISEE_VAR, OdiseeConstant.S_TEMPLATE)
        TEMPLATE_DIR.mkdir()
        // Documents
        DOCUMENT_DIR = new File(ODISEE_VAR, OdiseeConstant.S_DOCUMENT)
        DOCUMENT_DIR.mkdir()
        //
        dumpEnv()
    }

    static void dumpEnv() {
        // Dump environment to stdout
        println 'Odisee environment variables:'
        println "  ODISEE_HOME   =${ODISEE_HOME}"
        println "  ODISEE_DEPLOY =${ODISEE_DEPLOY}"
        println "  ODISEE_VAR    =${ODISEE_VAR}"
        println "  TEMPLATE_DIR  =${TEMPLATE_DIR}"
        println "  DOCUMENT_DIR  =${DOCUMENT_DIR}"
        println "  ODISEE_DEBUG  =${ODISEE_DEBUG}"
        println "  ODISEE_PROFILE=${ODISEE_PROFILE}"
    }

}