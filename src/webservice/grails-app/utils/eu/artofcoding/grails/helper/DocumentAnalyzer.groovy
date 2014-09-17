/*
 * odisee2
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 13.09.14 19:52
 */

package eu.artofcoding.grails.helper

import static eu.artofcoding.odisee.server.OdiseeConstant.*

class DocumentAnalyzer {

    static String guessContentType(String contentName) {
        String contentType
        switch (contentName) {
            case { it.endsWith('.odt') }:
                contentType = MIME_TYPE_ODT
                break
            case { it.endsWith('.pdf') }:
                contentType = MIME_TYPE_PDF
                break
            default:
                contentType = MIME_TYPE_OCTET_STREAM
        }
        contentType
    }

}
