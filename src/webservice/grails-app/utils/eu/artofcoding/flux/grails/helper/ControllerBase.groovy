/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.flux.grails.helper

import grails.converters.JSON
import java.util.zip.GZIPOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Base class for our controllers.
 * @author rbe
 */
abstract class ControllerBase {

    /**
     * Characters unwanted in request parameters.
     */
    String[] requestParameterChars = ('a'..'z').toList() + ('A'..'Z').toList() + (0..9).toList() + '%'

    /**
     *
     */
    void dumpconfig() {
        StringBuilder sb = new StringBuilder()
        Map c = grailsApplication.config
        c.each { k, v ->
            sb << "${k} = ${v}<br/>\n"
        }
        render sb.toString()
    }

    /**
     * Check characters of input against list of eligible characters.
     * @param input
     * @return
     */
    String checkRequestParameterChars(String input) {
        String output = input.collect { c ->
            if (c in requestParameterChars) {
                c
            } else {
                ''
            }
        }.join('')
        return output
    }

    /**
     * Compress a byte[] using gzip.
     * @param b The byte[] to compress.
     * @params base64 Encode bytes as base64?
     * @return byte[]
     */
    byte[] zip(byte[] b, boolean base64 = false) {
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream()
        GZIPOutputStream zipStream = new GZIPOutputStream(targetStream)
        zipStream.write(b)
        zipStream.close()
        if (base64) {
            byte[] zipped = targetStream.toByteArray()
            targetStream.close()
            return zipped.encodeBase64()
        } else {
            return targetStream.toByteArray()
        }
    }

    /**
     * Convenience method for: {@link #zip(byte [])}
     * @param str The string to compress using gzip.
     * @return byte[]
     */
    byte[] zip(String str) {
        return zip(str.bytes)
    }

    /**
     * Render a string, set headers accordingly.
     * @param response
     * @param str
     * @param characterSet The character set to use when encoding the string, standard is UTF-8.
     */
    void streamString(HttpServletResponse response, String str, String characterSet = 'UTF-8') {
        byte[] msg = str.getBytes(characterSet)
        response.contentLength = msg.length
        response.outputStream << msg
    }

    /**
     *
     * @param data
     */
    void renderAnswerAsJSON(HttpServletResponse response, data, contentType = 'text/json') {
        // Default map
        Map json = [success: [number: 0, message: 'No message.'], error: [number: 0, message: 'No message.']]
        // Success
        if (data.success) {
            json.success += data.success
        } else {
            json.remove('success')
        }
        // Error
        if (data.error) {
            json.error += data.error
        } else {
            json.remove('error')
        }
        // Write answer to stream
        response.contentType = contentType
        /*
         new StringWriter().with { writer ->
         StreamingJsonBuilder jsonBuilder = new StreamingJsonBuilder(writer, json)
         response.contentLength = writer.buffer.length()
         response.writer.write(writer.toString())
         }
         */
        /*
         JsonBuilder jsonBuilder = new JsonBuilder(json)
         response.contentLength = jsonBuilder.toString().length()
         response.writer.write(jsonBuilder.toString())
         */
        render json as JSON
    }

    /**
     * Stream an image, set headers accordingly.
     * @param response
     * @param image
     */
    void streamImage(HttpServletResponse response, File file) {
        try {
            if (file.exists() && file.canRead()) {
                // Decompose filename
                Map decomposedFilename = FileHelper.decomposeFilename(file)
                // Set content type
                String contentType = null
                switch (decomposedFilename.ext) {
                    case 'tif':
                        contentType = "tiff"
                        break
                    case 'jpg':
                        contentType = "jpeg"
                        break
                    default:
                        contentType = decomposedFilename.ext.toLowerCase()
                }
                response.contentType = "image/${contentType}"
                // Just stream 'inline'
                // BUG Chrome 16.0.912.75: No comma in filename
                String fname = decomposedFilename.name.replaceAll(',', '') // TODO Move into filter
                response.setHeader("Content-Disposition", "inline; filename=${fname}.${contentType}")
                // Push bytes to stream
                response.contentLength = file.size()
                response.outputStream << file.bytes
            } else {
                // TODO Stream error image
                //response.outputStream << new File().bytes
            }
        } catch (Exception e) {
            e.printStackTrace()
            // TODO Stream error image
            //response.outputStream << new File().bytes
        } finally {
            // Flush stream
            response.outputStream.flush()
        }
    }

    /**
     * Stream content (using gzip compression).
     * @param request
     * @param response
     * @param content
     */
    void stream(HttpServletRequest request, HttpServletResponse response, byte[] content) {
        // Does user's browser accept gzip encoding?
        boolean gzipCompressionAccepted = request.getHeader('Accept-Encoding').contains('gzip')
        if (content) {
            if (gzipCompressionAccepted) {
                byte[] z = zip(content)
                if (z) {
                    // Modify Content-Encoding header
                    response.setHeader('Content-Encoding', 'gzip')
                    // Set content length
                    response.contentLength = z.length
                    // Stream gzipped data
                    response.outputStream << z
                }
            } else {
                // Do not modify Content-Encoding header
                // Set content length
                response.contentLength = content.length
                // Stream data
                response.outputStream << content
            }
        }

    }

}
