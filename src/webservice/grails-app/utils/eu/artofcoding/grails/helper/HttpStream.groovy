/*
 * odisee2
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 13.09.14 20:21
 */

package eu.artofcoding.grails.helper

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpStream {

    /**
     * Stream content (using gzip compression).
     * @param request
     * @param response
     * @param content
     */
    static void stream(HttpServletRequest request, HttpServletResponse response, byte[] content) {
        // Does user's browser accept gzip encoding?
        boolean gzipCompressionAccepted = request.getHeader('Accept-Encoding').contains('gzip')
        if (content) {
            if (gzipCompressionAccepted) {
                byte[] z = zip(content)
                if (z) {
                    response.setHeader('Content-Encoding', 'gzip')
                    response.contentLength = z.length
                    response.outputStream << z
                }
            } else {
                response.contentLength = content.length
                response.outputStream << content
            }
        }
    }

    /**
     * Stream an image, set headers accordingly.
     * @param response
     * @param image
     */
    static void streamImage(HttpServletResponse response, File file) {
        try {
            if (file.exists() && file.canRead()) {
                Map decomposedFilename = FileHelper.decomposeFilename(file)
                String contentType
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
                // BUG Chrome 16.0.912.75: No comma in filename
                String fname = decomposedFilename.name.replaceAll(',', '')
                response.setHeader("Content-Disposition", "inline; filename=${fname}.${contentType}")
                response.contentLength = file.size()
                response.outputStream << file.bytes
            } else {
                // TODO Stream error image
                //response.outputStream << Files.readAllBytes()
            }
        } catch (Exception e) {
            e.printStackTrace()
            // TODO Stream error image
            //response.outputStream << Files.readAllBytes()
        } finally {
            response.outputStream.flush()
        }
    }

}
