/*
 * Odisee(R)
 *
 * Copyright (C) 2011-2015 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 09.11.14 11:19
 */

package eu.artofcoding.grails.helper

import static eu.artofcoding.odisee.server.OdiseeConstant.*

final class DocumentAnalyzer {

    static String guessContentType(final String contentName) {
        String contentType
        switch (contentName) {
            case { it.endsWith('.odt') }:
                contentType = MIME_TYPE_ODT
                break
            case { it.endsWith('.pdf') }:
                contentType = MIME_TYPE_PDF
                break
            case { it.endsWith('.docx') }:
                contentType = MIME_TYPE_WORDXML
                break
            case { it.endsWith('.doc') }:
                contentType = MIME_TYPE_WORD97
                break
            default:
                contentType = MIME_TYPE_OCTET_STREAM
        }
        contentType
    }

}
