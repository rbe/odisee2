/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.ControllerHelper
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseeWebserviceConstant
import groovy.xml.dom.DOMCategory

/**
 *
 */
class DocumentController {

    /**
     * The document storage service.
     */
    StorageService storageService

    /**
     * The Odisee service.
     */
    OdiseeService odiseeService

    /**
     * Default options for GORM.
     */
    private static final Map DEFAULT_LIST_OPTION = [sort: 'id', order: 'desc', max: 25]

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
     * TODO Enterprise feature
     * List last 25 templates/documents.
     def list() {[
     documents: OooDocument.list(DEFAULT_LIST_OPTION),
     totalDocuments: OooDocument.count()
     ]}*/

    /**
     * TODO Enterprise feature
     * List last 25 documents.
     def listDocuments() {[
     documents: OooDocument.findAllByTemplate(false, DEFAULT_LIST_OPTION),
     totalDocuments: OooDocument.countByTemplate(false)
     ]}*/

    /**
     * TODO Enterprise feature
     * List last 25 templates.
     def listTemplates() {[
     templates: OooDocument.findAllByTemplate(true, DEFAULT_LIST_OPTION),
     totalTemplates: OooDocument.countByTemplate(true)
     ]}*/

    /**
     * TODO Enterprise feature
     * Add a document to document service.
     def add() {if (params.name && params.url) {storageService.addDocument(file: params.name, data: params.url)
     redirect(action: 'list')} else if (params.file) {def f = request.getFile('file')
     storageService.addDocument(filename: f.originalFilename, data: f.bytes)
     redirect(action: 'list')}}*/

    /**
     * TODO Enterprise feature
     * Remove a document from document service.
     def remove() {// Remove document
     try {if (params.id) {storageService.remove(id: params.id)} else {log.error "ODI-xxxx: Missing parameter 'id' to remove a document"}} catch (e) {log.error "ODI-xxxx: Cannot remove document #${params.id}", e}// Redirect to list
     redirect(action: 'index')}*/

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
                    Map result = odiseeService.generateWithXml(request.userPrincipal, request.XML)
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
            /*
            else if (params.template) { // Generate a document using a template and value(s) from request parameters.
                // Check and correct stream parameters
                Map params = checkStreamParameter(params)
                // Generate document(s)
                List<OooDocument> document = odiseeService.generateWithProperties(params)
                // Stream document
                streamRequestedDocument(params, document)
            }
            */
            /*
            else {
                error.message = 'ODI-xxxx: Insufficient parameters'
            }
            */
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
        boolean shouldStream = !Boolean.valueOf(params.remove(OdiseeWebserviceConstant.S_NOSTREAM) ?: OdiseeWebserviceConstant.S_FALSE)
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
        params.outputFormat = /*params.*/ outputFormat ?: OdiseeWebserviceConstant.S_PDF
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
                if (result.document instanceof List) {
                    // Find document by 'outputFormat'
                    oooDocument = result.document.find {
                        it.mimeType?.name == params.outputFormat || it.filename?.endsWith(params.outputFormat)
                    } as OooDocument
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
