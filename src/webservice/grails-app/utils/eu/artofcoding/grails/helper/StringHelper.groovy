/*
 * Odisee(R)
 *
 * Copyright (C) 2011-2015 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 08.11.14 14:50
 */
package eu.artofcoding.grails.helper

final class StringHelper {

    /**
     * Uppercase first letter and add the rest.
     */
    def ucFirst(s) {
        if (s?.length() > 2) {
            s[0].toUpperCase() + s.substring(1)
        } else
            s
    }

    /**
     * Lowercase first letter and add the rest.
     */
    def lcFirst(s) {
        if (s?.length() > 2) {
            s[0].toLowerCase() + s.substring(1)
        } else
            s
    }

    /**
     * Count spaces at the beginning of a string.
     * @param what String to count.
     * @return int Number of leading spaces found.
     */
    public static int countSpacesAtBeginning(final String what) {
        boolean charSeen = false
        what.inject 0, { o, n ->
            if (!charSeen) {
                charSeen = n != ' '
            }
            if (!charSeen && n == ' ') {
                o + 1
            } else {
                o
            }
        }
    }

}
