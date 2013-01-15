/*
 * odisee
 * webservice
 * Copyright (C) 2011-2013 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 13.11.12 16:54
 */
package eu.artofcoding.odisee

import static eu.artofcoding.odisee.server.OdiseeConstant.*

/**
 * Odisee's paths, built from environment variables.
 */
@Singleton
public class OdiseePath {

    /**
     * Profile?
     */
    public static final Boolean ODISEE_PROFILE = Boolean.valueOf(System.getenv(S_ODISEE_PROFILE) ?: S_FALSE)

    /**
     * Debug?
     */
    public static final Boolean ODISEE_DEBUG = Boolean.valueOf(System.getenv(S_ODISEE_DEBUG) ?: S_FALSE)

    /**
     * Home: installation path of OpenOffice.
     */
    public static final String OOO_HOME = System.getenv('OOO_HOME')

    /**
     * Base directory for Odisee.
     */
    public static final File ODISEE_HOME

    /**
     * Deployment directory for Odisee, contains additional files.
     */
    public static final File ODISEE_DEPLOY

    /**
     * Variable data directory for Odisee.
     */
    public static final File ODISEE_VAR
    public static final File ODISEE_USER
    public static final File ODISEE_TMP

    static {
        // ODISEE_HOME
        // Is Odisee home set?
        String envOdiseeHome = System.getenv(S_ODISEE_HOME)
        if (!envOdiseeHome) {
            ODISEE_HOME = new File(S_DOT).absoluteFile
            println "Please set ODISEE_HOME, using actual directory ${ODISEE_HOME}"
        } else {
            ODISEE_HOME = new File(envOdiseeHome).absoluteFile
        }
        //
        // ODISEE_DEPLOY
        String envOdiseeDeploy = System.getenv(S_ODISEE_DEPLOY)
        if (envOdiseeDeploy) {
            ODISEE_DEPLOY = new File(envOdiseeDeploy).absoluteFile
        } else {
            ODISEE_DEPLOY = new File(ODISEE_HOME, S_VAR_DEPLOY).absoluteFile
        }
        ODISEE_DEPLOY.mkdirs()
        //
        // ODISEE_VAR
        String envOdiseeVar = System.getenv(S_ODISEE_VAR)
        if (envOdiseeVar) {
            ODISEE_VAR = new File(envOdiseeVar).absoluteFile
        } else {
            ODISEE_VAR = new File(ODISEE_HOME, S_VAR).absoluteFile
        }
        ODISEE_VAR.mkdirs()
        //
        // ODISEE_USER
        String envOdiseeUser = System.getenv(S_ODISEE_USER)
        if (envOdiseeUser) {
            ODISEE_USER = new File(envOdiseeUser).absoluteFile
        } else {
            ODISEE_USER = new File(ODISEE_VAR, S_USER).absoluteFile
        }
        ODISEE_USER.mkdirs()
        //
        // ODISEE_TMP
        String envOdiseeTmp = System.getenv(S_ODISEE_TMP)
        if (envOdiseeTmp) {
            ODISEE_TMP = new File(envOdiseeTmp).absoluteFile
        } else {
            ODISEE_TMP = new File(ODISEE_HOME, S_VAR_TMP).absoluteFile
        }
        ODISEE_TMP.mkdirs()
        //
        dumpEnv()
    }

    static void dumpEnv() {
        // Dump environment to stdout
        println 'Odisee environment variables:'
        println "  ODISEE_HOME   =${ODISEE_HOME}"
        println "  ODISEE_DEPLOY =${ODISEE_DEPLOY}"
        println "  ODISEE_VAR    =${ODISEE_VAR}"
        println "  ODISEE_USER   =${ODISEE_USER}"
        println "  ODISEE_TMP    =${ODISEE_TMP}"
        if (ODISEE_DEBUG) {
            println "  ODISEE_DEBUG  =${ODISEE_DEBUG}"
        }
        if (ODISEE_PROFILE) {
            println "  ODISEE_PROFILE=${ODISEE_PROFILE}"
        }
    }

}
