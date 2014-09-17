/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.grails.helper

import eu.artofcoding.odisee.document.Document

import javax.servlet.http.HttpServletResponse
import java.security.SecureRandom

class DocumentStreamer {

    /**
     * Stream a Document or just bytes to client.
     * @param arg
     */
    static void stream(HttpServletResponse response, Document document) {
        String contentFilename = document.filename
        if (contentFilename) {
            response.contentType = DocumentAnalyzer.guessContentType(contentFilename)
        } else {
            int nextInt = new SecureRandom().nextInt((int) System.currentTimeMillis())
            contentFilename = String.format('Odisee_%s', nextInt)
        }
        response.setHeader('Content-disposition', "inline; filename=${contentFilename}")
        if (document.size) {
            response.contentLength = document.size
        }
        response.outputStream << document.bytes
        response.outputStream.flush()
    }

}
