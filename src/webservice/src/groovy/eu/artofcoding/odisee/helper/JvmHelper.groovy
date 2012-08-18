/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
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
