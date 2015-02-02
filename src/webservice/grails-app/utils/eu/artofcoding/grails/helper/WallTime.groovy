/*
 * Odisee(R)
 *
 * Copyright (C) 2011-2015 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 01.02.15 17:42
 */

package eu.artofcoding.grails.helper

final class WallTime {

    private long startMilliseconds

    private long stopMilliseconds

    def start() {
        startMilliseconds = System.currentTimeMillis()
    }

    def stop() {
        stopMilliseconds = System.currentTimeMillis()
    }

    long diff() {
        stopMilliseconds - startMilliseconds
    }

}
