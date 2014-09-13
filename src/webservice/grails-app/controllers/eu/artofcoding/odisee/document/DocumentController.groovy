/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.ControllerHelper
import eu.artofcoding.grails.helper.WallTime
import eu.artofcoding.grails.helper.XmlHelper
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseePath
import groovy.util.slurpersupport.GPathResult
import groovy.xml.dom.DOMCategory
import org.w3c.dom.Element

import static eu.artofcoding.odisee.server.OdiseeConstant.*

class DocumentController {

    /**
     * The Odisee service.
     */
    OooService oooService

    /**
     * The before-request-interceptor.
     */
    def beforeInterceptor = {
        // Don't cache our response.
        response.setHeader('Cache-Control', 'no-cache,no-store,must-revalidate,max-age=0')
    }

    /**
     * Generate document with values from XML request and an OpenOffice template.
     */
    def generate() {
        try {
            GPathResult xml = request.XML
            if (xml) {
                WallTime wallTime = new WallTime()
                if (OdiseePath.ODISEE_PROFILE) {
                    wallTime.start()
                }
                try {
                    // Build Odisee XML request using StreamingMarkupBuilder
                    Element documentElement = XmlHelper.asElement(xml)
                    Map result = oooService.generateDocument(principal: request.userPrincipal, xml: documentElement)
                    streamRequestedDocument(result)
                } catch (e) {
                    ControllerHelper.sendNothing(log: log, response: response, message: "ODI-xxxx: Document generation failed: ${e.message}", exception: e)
                } finally {
                    if (OdiseePath.ODISEE_PROFILE) {
                        wallTime.stop()
                        log.info "ODI-xxxx: Document processing took ${wallTime.diff()} ms (wall clock)"
                    }
                }
            } else {
                ControllerHelper.sendNothing(log: log, response: response, message: 'ODI-xxxx: No request found')
            }
        } catch (e) {
            ControllerHelper.sendNothing(log: log, response: response, message: 'ODI-xxxx: Cannot fulfill request', exception: e)
        }
        // Prevent Grails from rendering generate.gsp (it does not exist)
        response.outputStream.close()
    }

    /**
     * Stream a certain document depending on request parameters.
     * @param params Request parameter
     * @param document Result from oooService.generateWithProperties()
     */
    private void streamRequestedDocument(result) {
        String outputFormat = ''
        use(DOMCategory) {
            def request = result.arg.xml.request[result.arg.activeIndex]
            def template = request.template[0]
            outputFormat = template.'@outputFormat'?.toString()?.split(',')?.first()
        }
        if (!outputFormat || outputFormat.length() == 0) {
            if (log.warnEnabled) {
                log.warn "ODI-xxxx: No outputFormat defined, not sending anything"
            }
            return
        }
        try {
            // Stream document
            OooDocument oooDocument = null
            // Find document to stream
            if (result.document instanceof List && result.document.size() > 0) {
                oooDocument = result.document.last()
            }
            if (null != oooDocument) {
                ControllerHelper.stream(log: log, response: response, document: oooDocument)
            } else {
                throw new OdiseeException('ODI-xxxx: Cannot send stream, no document generated')
            }
        } catch (e) {
            ControllerHelper.sendNothing(log: log, response: response, message: e.message, exception: e)
        }
    }

}
