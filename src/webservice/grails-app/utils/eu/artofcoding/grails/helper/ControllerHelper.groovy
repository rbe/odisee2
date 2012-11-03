/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.grails.helper

import eu.artofcoding.odisee.document.OooDocument
import java.security.SecureRandom
import javax.servlet.http.HttpServletResponse
import eu.artofcoding.odisee.OdiseeWebserviceConstant

/**
 * A helper for controllers.
 */
class ControllerHelper {

    /**
     *
     * @param arg
     */
    static void sendNothing(arg) {
        try {
            // TODO Enable or disable logging of errors as they already are displayed in HTTP response instead of a document
            arg.log.error arg.message, arg.exception
            HttpServletResponse response = arg.response
            response.reset()
            response.outputStream << String.format('Sorry, I have got no document for you, %s%n', arg.message)
            response.outputStream.flush()
        } catch (e) {
            arg.log.error 'ODI-xxxx: Could not send error message to client', e
        }
    }

    /**
     * Stream a OooDocument or just bytes to client.
     * @param arg
     */
    static void stream(arg) {
        byte[] bytes = null
        HttpServletResponse response = arg.response
        def document = arg.document
        String contentType
        long contentLength
        String contentName
        // OooDocument
        if (document instanceof OooDocument) {
            //contentType = document.mimeType.browser ?: OdiseeWebserviceConstant.MIME_TYPE_OCTET_STREAM
            contentLength = document.data?.length() ?: document.bytes?.length
            contentName = document.filename
            if (document.data) {
                bytes = document.data?.getBytes(1L, document.data?.length().toInteger())
            } else if (document.bytes) {
                bytes = document.bytes
            }
        } else if (document instanceof byte[]) { // byte[]
            //contentType = OdiseeWebserviceConstant.MIME_TYPE_OCTET_STREAM
            contentLength = document.length
            contentName = "file_${new SecureRandom().nextInt(System.currentTimeMillis())}"
            bytes = document
        } else if (document instanceof List) { // List of documents
            // Find document to stream by request parameter 'type'
            def d = document.find {
                it.mimeType?.name == arg.params.streamtype || it.filename.endsWith(arg.params.streamtype)
            }
            //contentType = d.mimeType.browser
            bytes = d.toByteArray()
            contentLength = bytes.length
            contentName = d.filename ?: d.name
        } else { // No data message
            contentType = 'text/plain'
            bytes = 'No data'.bytes
            contentLength = bytes.length
        }
        // Stream to client
        if (bytes) {
            switch (contentName) {
                case { it.endsWith('.odt') }:
                    contentType = OdiseeWebserviceConstant.MIME_TYPE_ODT
                    break
                case { it.endsWith('.pdf') }:
                    contentType = OdiseeWebserviceConstant.MIME_TYPE_PDF
                    break
                default:
                    contentType = OdiseeWebserviceConstant.MIME_TYPE_OCTET_STREAM
            }
            // Content type and length
            response.contentType = contentType
            if (contentLength) {
                response.contentLength = contentLength
            }
            // Content disposition
            if (contentName) {
                String cd = arg.contentDisposition ?: 'inline'
                response.setHeader('Content-disposition', "${cd}; filename=${contentName}")
            }
            // Append bytes to output stream
            response.outputStream << bytes
            // Flush stream
            response.outputStream.flush()
        }
    }

}
