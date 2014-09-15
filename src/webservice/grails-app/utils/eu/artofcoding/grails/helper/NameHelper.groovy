/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.grails.helper

import static eu.artofcoding.odisee.server.OdiseeConstant.S_DOT

class NameHelper {

    /**
     * Get name of document w/o extension.
     * @param filename
     * @return
     */
    static String getName(String filename) {
        String[] s = filename.split('\\.')
        s[0..s.length - 2].join(S_DOT)
    }

    /**
     * HelloWorld -> Hello_World
     * helloWorld -> hello_World
     */
    static String mapCamelCaseToUnderscore(s) {
        def buf = new StringBuilder()
        s.eachWithIndex { c, i ->
            if (i > 0 && Character.isUpperCase((char) c)) {
                buf << "_"
            }
            buf << c
        }
        buf.charAt(0) == "_" ? buf.substring(1) : buf.toString()
    }

    /**
     * Hello_World -> HelloWorld
     * hello_World -> helloWorld
     */
    static String mapUnderscoreToCamelCase(s) {
        def buf = new StringBuilder()
        s.eachWithIndex { c, i ->
            if (c == "_") {
            } else if (i > 0 && s.charAt(i - 1) == "_") {
                buf << c.toUpperCase()
            } else {
                buf << c
            }
        }
        buf.toString()
    }

    /**
     * Hello-World -> HelloWorld
     * hello-World -> helloWorld
     */
    static String mapDashToCamelCase(s) {
        def buf = new StringBuilder()
        s.eachWithIndex { c, i ->
            if (c == "-") {
            } else if (i > 0 && s.charAt(i - 1) == "-") {
                buf << c.toUpperCase()
            } else {
                buf << c
            }
        }
        buf.toString()
    }

}
