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
import eu.artofcoding.grails.helper.XmlHelper
import eu.artofcoding.odisee.OdiseeException
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
     * The index action.
     */
    def index() {
        render 'Hello, this is the <a target="_blank" href="http://www.odisee.de/">Odisee</a> service.'
    }

    /**
     * Generate d document with values from XML request and an OpenOffice template.
     */
    def generate() {
        Map error = [:]
        try {
            // Generate document using posted XML request for Odisee
            if (request.XML) {
                // Stop processing time
                long startWallTime = System.currentTimeMillis()
                try {
                    // Build Odisee XML request using StreamingMarkupBuilder
                    Element documentElement = XmlHelper.asElement(request.XML)
                    Map result = oooService.generateDocument(principal: request.userPrincipal, xml: documentElement)
                    streamRequestedDocument(params, result)
                } catch (e) {
                    error.message = 'ODI-xxxx: Document generation failed'
                    error.exception = e
                } finally {
                    // Stop processing time
                    long stopWallTime = System.currentTimeMillis()
                    if (log.debugEnabled) {
                        log.debug "ODI-xxxx: Document processing took ${stopWallTime - startWallTime} ms (wall clock)"
                    }
                }
            }
        } catch (e) {
            error.message = 'ODI-xxxx: Cannot fulfill request'
            error.exception = e
        }
        // Stream data
        if (error.message) {
            ControllerHelper.sendNothing(log: log, response: response, message: error.message, exception: error.exception)
        }
        // Prevent Grails from rendering generate.gsp (it does not exist)
        response.outputStream.close()
    }

    /**
     * Stream a certain document depending on request parameters.
     * @param params Request parameter
     * @param document Result from oooService.generateWithProperties()
     */
    private void streamRequestedDocument(params, result) {
        // Should we stream the result back to client? Default is yes, but user can choose to not get a stream.
        boolean shouldStream = !Boolean.valueOf(params.remove(S_NOSTREAM) ?: S_FALSE)
        // Get XML request element
        String outputFormat = ''
        use(DOMCategory) {
            def request = result.arg.xml.request[result.arg.activeIndex]
            def template = request.template[0]
            outputFormat = template.'@outputFormat'?.toString()?.split(',')?.first()
        }
        if (!outputFormat || outputFormat.length() == 0) {
            return
        }
        // Check parameter: document type defaults to PDF.
        params.outputFormat = /*params.*/ outputFormat ?: S_PDF
        ////params.streamtype = params.streamtype ?: params.outputFormat
        /*
        // TODO Multiple requests... watch for tag post-process.
        // Set stream type for streamRequestedDocument() by content of XML attribute template/@outputFormat
        String xmlOutputFormat = request.XML.request.template.'@outputFormat'?.text()
        if (xmlOutputFormat) {
            // Multiple output formats can be requested, e,g, odt,pdf
            // Stream back last one
            params.streamtype = xmlOutputFormat.split(',')[-1]
        }
        */
        // Stream document
        OooDocument oooDocument = null
        try {
            // Send answer
            if (shouldStream) {
                // Find document to stream
                // TODO stream-by-name (e.g. for merged documents or multiple-requests), use <stream/> in request XML to mark resulting document as stream-it-back
                if (result.document instanceof List && result.document.size() > 0) {
                    /*
                    // Find document by 'outputFormat'
                    oooDocument = result.document.find {
                        it.mimeType?.name == params.outputFormat || it.filename?.endsWith(params.outputFormat)
                    } as OooDocument
                    */
                    oooDocument = result.document.last()
                }
                if (null != oooDocument) {
                    if (log.debugEnabled) {
                        log.debug "ODI-xxxx: streaming document id=${oooDocument.id} name=${oooDocument.filename}"
                    }
                    ControllerHelper.stream(log: log, response: response, document: oooDocument)
                } else {
                    throw new OdiseeException('ODI-xxxx: No document generated')
                }
            }
        } catch (e) {
            ControllerHelper.sendNothing(log: log, response: response, message: e.message, exception: e)
        }
    }

    /**
     * Stream an already stored document from document service.
     def stream() {// Check stream and correct parameters
     def params = checkStreamParameter(params)
     // Document (will be streamed back)
     def document = null
     // Fetch document by id
     if (params.id) {if (log.debugEnabled) {log.debug "ODI-xxxx: Fetching document by ID=${params.id}"}document = OooDocument.get(params.id)} else if (params.name && params.revision) { // Fetch template by name and revision
     if (log.debugEnabled) {log.debug "ODI-xxxx: Fetching document by name=${params.name} and revision=${revision}"}document = storageService.getDocument(name: params.name, revision: params.revision, mimeType: params.mimetype)} else if (params.name) { // Fetch template/document by name
     if (log.debugEnabled) {log.debug "ODI-xxxx: Fetching document by name=${params.name} and latest revision"}document = storageService.getDocument(name: params.name, mimeType: params.mimetype)}// Stream document
     if (document) {ControllerHelper.stream(log: log, params: params, response: response, document: document)} else {ControllerHelper.sendNothing(log: log, response: response, message: "ODI-xxxx: Can't stream, no data. Maybe insufficient parameters? params=${params}")}}*/

}
