/*
 * odisee
 * webservice
 * Copyright (C) 2011-2013 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 03.11.12 14:05
 */
package eu.artofcoding.odisee.helper

/**
 *
 */
class JvmHelper {

    /**
     * Name of operating system.
     */
    public static final String OS_NAME = System.getProperty('os.name')
    public static final String OS_WINDOWS = /Windows.*/
    public static final String OS_DARWIN = /Mac OS.*/
    public static final String OS_LINUX = 'Linux'
    public static final String OS_SUNOS = 'SunOS'
    public static final String OS_BSD = /.*BSD/

}
