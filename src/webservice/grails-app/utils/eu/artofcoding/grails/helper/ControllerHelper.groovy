/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.grails.helper

import eu.artofcoding.odisee.document.OooDocument

import javax.servlet.http.HttpServletResponse
import java.security.SecureRandom

import static eu.artofcoding.odisee.server.OdiseeConstant.*

/**
 * A helper for controllers.
 */
class ControllerHelper {

    static void sendNothing(arg) {
        try {
            // TODO Enable or disable logging of errors as they already are displayed in HTTP response instead of a document
            arg.log.error arg.message, arg.exception
            HttpServletResponse response = arg.response
            response.reset()
            response.outputStream << String.format('%s%n', arg.message)
            response.outputStream.flush()
        } catch (e) {
            arg.log.error 'ODI-xxxx: Could not send error message to client', e
        }
    }

    private static String guessContentType(String contentName, String contentType) {
        switch (contentName) {
            case { it.endsWith('.odt') }:
                contentType = MIME_TYPE_ODT
                break
            case { it.endsWith('.pdf') }:
                contentType = MIME_TYPE_PDF
                break
        }
        contentType
    }

    private static List byteArray(byte[] document) {
        def contentLength = document.length
        def random = new SecureRandom().nextInt((int) System.currentTimeMillis())
        def contentName = "Document_${random}"
        def bytes = document
        [bytes, contentName, contentLength]
    }

    private static List oooDocument(OooDocument document) {
        def bytes = document.bytes
        def contentLength = document.size
        def contentName = document.filename
        if (document.data) {
            bytes = document.data?.getBytes(1L, document.data?.length()?.toInteger())
        } else if (document.bytes) {
            bytes = document.bytes
        }
        [bytes, contentName, contentLength]
    }

    private static List listOooDocument(List<OooDocument> document) {
        OooDocument d = (OooDocument) document[-1]
        def (bytes, contentName, contentLength) = oooDocument(d)
        [bytes, contentName, contentLength]
    }

    /**
     * Stream a OooDocument or just bytes to client.
     * @param arg
     */
    static void stream(arg) {
        HttpServletResponse response = arg.response
        // OooDocument
        def document = arg.document
        byte[] bytes = null
        String contentType = MIME_TYPE_OCTET_STREAM
        long contentLength = 0L
        String contentFilename = null
        if (document instanceof OooDocument) {
            (bytes, contentFilename, contentLength) = oooDocument(document)
        } else if (document instanceof byte[]) {
            (bytes, contentFilename, contentLength) = byteArray(document)
        } else if (document instanceof List) {
            (bytes, contentFilename, contentLength) = listOooDocument(document)
        }
        // Stream to client
        if (bytes) {
            contentType = guessContentType(contentFilename, contentType)
            // Content type and length
            response.contentType = contentType
            if (contentLength) {
                response.contentLength = contentLength
            }
            // Content disposition
            if (contentFilename) {
                String contentDisposition = arg.contentDisposition ?: 'inline'
                response.setHeader('Content-disposition', "${contentDisposition}; filename=${contentFilename}")
            }
            if (arg.log.debugEnabled) {
                arg.log.debug "ODI-xxxx: streaming document id=${arg.document.id} name=${arg.document.filename}"
            }
            // Append bytes to output stream
            response.outputStream << bytes
            // Flush stream
            response.outputStream.flush()
        }
    }

}
