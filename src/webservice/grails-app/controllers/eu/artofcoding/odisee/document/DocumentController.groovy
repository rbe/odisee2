/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.Compression
import eu.artofcoding.grails.helper.DocumentStreamer
import eu.artofcoding.grails.helper.WallTime
import eu.artofcoding.grails.helper.XmlHelper
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseePath
import org.w3c.dom.Element

class DocumentController {

    /**
     * The Odisee service.
     */
    OdiseeService odiseeService

    /**
     * The before-request-interceptor:
     * Don't cache our response.
     */
    def beforeInterceptor = {
        response.setHeader('Cache-Control', 'no-cache,no-store,must-revalidate,max-age=0')
    }

    def generate() {
        WallTime wallTime = new WallTime()
        if (OdiseePath.ODISEE_PROFILE) {
            wallTime.start()
        }
        try {
            Element xml = postBodyToXml()
            if (null != xml) {
                Document document = processXmlRequest(xml)
                if (null == document) {
                    throw new OdiseeException('ODI-xxxx: Cannot send stream, no document')
                } else {
                    DocumentStreamer.stream(response, document)
                }
            } else {
                throw new OdiseeException('ODI-xxxx: Invalid or missing XML request')
            }
        } catch (e) {
            processThrowable(e)
        } finally {
            // Prevent Grails from rendering generate.gsp (it does not exist)
            response.outputStream.close()
            if (OdiseePath.ODISEE_PROFILE) {
                wallTime.stop()
                log.info "ODI-xxxx: Document processing took ${wallTime.diff()} ms (wall clock)"
            }
        }
    }

    /**
     * Parse POST body: can be just text or gzip'ed stream.
     * Does not use request.XML as it relies on HTTP request headers.
     */
    private Element postBodyToXml() {
        InputStream postBody = Compression.decompressStream(request.inputStream)
        List<String> lines = postBody.readLines('UTF-8')
        Element xml = XmlHelper.asElement(lines)
        xml
    }

    private Document processXmlRequest(Element xml) {
        try {
            List<Document> documents = odiseeService.generateDocument(request.userPrincipal, xml)
            if (null != documents && documents.size() > 0) {
                Document document = documents.last()
                return document
            } else {
                throw new OdiseeException('ODI-xxxx: Document generation failed')
            }
        } catch (e) {
            throw new OdiseeException("ODI-xxxx: Document generation failed, got ${e.getCause().getClass().getSimpleName()}")
        }
    }

    /**
     * Handle an exception: extract message and write response to client.
     * @param throwable The exception to handle.
     */
    private void processThrowable(Throwable throwable) {
        try {
            String msg
            if (null != throwable) {
                msg = throwable.message
                log.error msg, throwable
            }
            response.reset()
            if (null != msg) {
                response.outputStream << String.format('%s%n', msg)
            }
            response.outputStream.flush()
        } catch (e) {
            log.error 'ODI-xxxx: Could not send error message to client', e
        }
    }

}
