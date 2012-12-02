/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.NameHelper
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseeXmlCategory
import eu.artofcoding.odisee.server.OdiseeConstant
import eu.artofcoding.odisee.server.OfficeConnectionFactory
import groovy.xml.dom.DOMCategory
import org.springframework.beans.factory.InitializingBean

import static eu.artofcoding.odisee.OdiseePath.ODISEE_HOME
import static eu.artofcoding.odisee.OdiseePath.ODISEE_USER
import static eu.artofcoding.odisee.server.OdiseeConstant.*

/**
 * A service that is a client for the OOo service. Provides access to standalone, embedded or webservice
 * version of the service.
 */
class OooService implements InitializingBean {

    /**
     * The scope. See http://www.grails.org/Services.
     * prototype request flash flow conversation session singleton
     */
    def scope = 'prototype'

    /**
     * Transactional?
     */
    boolean transactional = true

    /**
     * GrailsApplication.
     */
    def grailsApplication

    /**
     * Manager for connections to OpenOffice.
     */
    OfficeConnectionFactory officeConnectionFactory

    /**
     * The document service.
     */
    StorageService storageService

    /**
     * Service to post-process the result.
     */
    PostProcessService postProcessService

    /**
     * Initialize Odisee's connection manager.
     * Called after Spring bean was initialized... see org.springframework.beans.factory.InitializingBean.
     */
    @Override
    void afterPropertiesSet() {
        setupOooConnectionManager()
    }

    /**
     * Setup OOo connection manager.
     * @param force Force initialization of connection manager? Default is false.
     */
    private void setupOooConnectionManager(boolean force = false) {
        // Check state, do not initialize twice
        if (force || !officeConnectionFactory) {
            Map oooGroup = [:]
            Map ipPortGroup = [:]
            // Try to get etc/odiinst through environment variable ODISEE_HOME
            File odiinst = new File(ODISEE_HOME, 'etc/odiinst'.intern())
            // Read contents of odiinst
            // odiinst=[odi1, 127.0.0.1, 2001, /Applications/OpenOffice.org.app, /Users/rbe/project/odisee/var/user/odi1, nologo nofirststartwizard norestart nodefault nolockcheck nocrashreport, true]
            List<String> contents = []
            if (odiinst?.exists()) {
                // odi1|127.0.0.1|2001| ...
                odiinst.eachLine S_UTF8, {
                    contents << it.split('[|]')
                }
                // Group ports by IP address
                Map<String, List<String>> groupIpPort = contents.groupBy { it[1] }
                groupIpPort.each { k, v ->
                    ipPortGroup[k] = v.collect { it[2] }
                }
                ipPortGroup.eachWithIndex { it, i ->
                    Map m = [:]
                    m[it.key] = it.value
                    oooGroup[S_GROUP0] = m
                }
            } else {
                log.warn('ODI-xxxx: No odiinst found, using default 127.0.0.1:2001')
                // Setup defaults; port 2001 on localhost
                oooGroup[S_GROUP0] = ['127.0.0.1': 2001]
            }
            // Create and return instance of OOo connection manager
            try {
                String localhost = contents[0][1]
                int portbase = 2001
                officeConnectionFactory = OfficeConnectionFactory.getInstance(S_GROUP0, localhost, portbase, contents.size())
            } catch (e) {
                throw new IllegalStateException('Cannot setup Office connection factory, please check instance configuration', e)
            }
        }
    }

    /**
     * Convert a simple map with values for OpenOffice userfields (e.g. controller's parameters)
     * into properties for generating an OOo document.
     * @param arg
     * @return
     */
    Map mapToOooProps(Map arg) {
        // Create map with values for template
        Map prop = [id: [value: arg.map.id ?: 0]]
        arg.map.findAll { k, v ->
            // Skip Grails' controller parameters
            !(k in ['controller', 'action', OdiseeConstant.S_TEMPLATE])
        }?.each { k, v ->
            prop[k] = [
                    value: v,
                    postSetMacro: arg.map."${k}_postSetMacro"
            ]
        }
        prop
    }

    /**
     *
     * @param arg
     */
    private void prepareArg(Map arg) {
        // Check and extract some values
        use(DOMCategory) {
            def request = arg.xml.request[arg.activeIndex]
            def template = request.template[0]
            // Request ID
            if (!request.'@id') {
                request.setAttribute(S_ID, new Date().format('yyyyMMdd-HHmmss_SSSS'))
            }
            arg.id = request.'@id'.toString()
            // Name of template
            arg.template = template.'@name'
            if (!arg.template) {
                throw new OdiseeException('No template in request')
            }
            // Get (latest) revision of template?
            arg.revision = template.'@revision' ?: S_LATEST
            if (!arg.revision || arg.revision == S_LATEST) {
                arg.revision = storageService.getDocumentsLatestRevision(name: arg.template)
                // Save in XML request for Odisee
                template.setAttribute(S_REVISION, arg.revision.toString())
            }
        }
        // Check state
        boolean ok = arg.template && arg.revision && arg.id
        if (!ok) {
            throw new OdiseeException("No template (${arg.template}), revision (${arg.revision}) for request id ${arg.id}")
        }
    }

    /**
     * Check (and set) paths for template, output in request XML.
     * @param arg
     */
    private void checkPaths(Map arg) {
        use(DOMCategory) {
            def request = arg.xml.request[arg.activeIndex]
            def template = request.template[0]
            // Ensure template path in Odisee XML request
            template.setAttribute('path', arg.templateFile.absolutePath)
            // Ensure document output path for Odisee XML request
            if (!template.'@outputPath') {
                template.setAttribute('outputPath', arg.documentDir.toString())
            }
            // The name of the generated document
            if (request.'@name') {
                arg.documentName = request.'@name'
            } else {
                arg.documentName = "${arg.template}_rev${arg.revision}_id${arg.id}"
            }
        }
    }

    /**
     * Save XML request to disk (to use with OdiseeXmlCategory), process request and set arg.result.
     * @param arg Map
     */
    private void processSingleRequest(Map arg) {
        // Save (enriched) XML request to disk
        File requestXMLFile = storageService.saveRequestToDisk(arg, arg.activeIndex)
        // Process request
        use(OdiseeXmlCategory) {
            def request = arg.xml.request[arg.activeIndex]
            //arg.result = requestXMLFile.toDocument(oooConnectionManager, 0) // requestNumber = 0 as file contains only one request
            arg.result = requestXMLFile.toDocument(officeConnectionFactory, 0) // requestNumber = 0 as file contains only one request
            if (!arg.result) {
                // This should never happen; except all OOo instances have crashed.
                String group = '?'
                try {
                    group = 'group0' // TODO request.ooo[0]?.'@group'
                } catch (e) {
                    // ignore
                }
                log.error "ODI-xxxx: ${requestXMLFile.name}/${arg.activeIndex}: Got no result, maybe all OpenOffice instance(s) in group '${group}' are unwilling to perform?"
            }
        }
    }

    /**
     * Delegate post-process instructions to PostProcessService.
     * @param arg
     * @return
     */
    private void postProcessRequest(Map arg) {
        use(DOMCategory) {
            def postProcess = arg.xml.request[arg.activeIndex].'post-process'
            String methodName = null
            postProcess.instructions.'*'.eachWithIndex { instr, idx ->
                // Construct method name from element name
                methodName = instr.'@type'[0].toUpperCase() + NameHelper.mapDashToCamelCase(instr.'@type')[1..-1]
                postProcessService."process${methodName}"(arg, instr)
            }
        }
    }

    /**
     *
     * @param arg
     */
    private void postProcessAll(Map arg) {
        use(DOMCategory) {
            def postProcess = arg.xml.'post-process'
            String methodName = null
            postProcess.instructions.'*'.eachWithIndex { instr, idx ->
                // Construct method name from element name
                methodName = instr.'@type'[0].toUpperCase() + NameHelper.mapDashToCamelCase(instr.'@type')[1..-1]
                postProcessService."process${methodName}"(arg, instr)
            }
        }
    }

    /**
     * Process result.
     * @param arg
     */
    private void processSingleResult(Map arg) {
        if (arg.result) {
            // Post-process request
            postProcessRequest(arg)
            // Archive generated document(s)?
            use(DOMCategory) {
                def request = arg.xml.request[arg.activeIndex]
                def archive = request.archive[0]
                // Archive in database: request XML as string, generated document as BLOB?
                if (archive?.'@database' == S_TRUE) {
                    archive(arg)
                } else {
                    // Just create generated document as OooDocument bean.
                    arg.result.output.each { file ->
                        arg.document << storageService.createDocument(data: file)
                    }
                }
            }
        } else {
            throw new OdiseeException('ODI-xxxx: No result!')
        }
    }

    /**
     * Archive document(s).
     * @param arg Map: result: filename -> bytes
     * @return Array of OooDocument instance(s).
     */
    private void archive(Map arg) {
        use(DOMCategory) {
            def request = arg.xml.request[arg.activeIndex]
            def archive = request.archive[0]
            if (log.debugEnabled && archive) {
                log.debug "ODI-xxxx: archive: files=${archive.'@files'}, database=${archive.'@database'}"
            } else {
                log.debug 'ODI-xxxx: No archive tag found'
            }
            OooDocument oooDocument = null
            arg.result.output.each { k, v ->
                try {
                    // TODO Move feature into enterprise version of Odisee
                    oooDocument = storageService.addDocument(
                            instanceOf: [name: arg.template, revision: arg.revision],
                            filename: k.name,
                            odiseeRequest: arg.xmlString,
                            data: v
                    )
                    if (oooDocument) {
                        arg.document << oooDocument
                        if (log.debugEnabled) {
                            log.debug "ODI-xxxx: Document archived: ${oooDocument}"
                        }
                    } else {
                        log.error "ODI-xxxx: Couldn't archive document from request ${request.'@name'}"
                    }
                } catch (e) {
                    // Ignore exception
                    log.error "ODI-xxxx: Couldn't archive document ${oooDocument}", e
                }
            }
        }
    }

    /**
     * Delete working directory?
     * @param arg
     */
    private void cleanupWorkingDirectory(Map arg) {
        use(DOMCategory) {
            def request = arg.xml.request[arg.activeIndex]
            def archive = request.archive[0]
            boolean archiveFiles = archive?.'@files' == S_TRUE
            // TODO Check if we override this setting
            // Delete files when <archive files="false">
            if (!archiveFiles) {
                try {
                    boolean b = arg.documentDir.deleteDir()
                    if (log.debugEnabled) {
                        log.debug "ODI-xxxx: cleanupWorkingDirectory: Deleting document directory ${arg.documentDir} was ${b ? 'successful' : 'unsuccessful'}"
                    }
                } catch (e) {
                    log.error "ODI-xxxx: cleanupWorkingDirectory: Couldn't delete working directory ${arg.documentDir}", e
                }
            }
        }
    }

    /**
     * Generate a document using document service and OOo service.
     * @param arg Map: xml: an XML request (see request.xsd in Odisee).
     * @return List with generated OooDocument instance(s).
     */
    Map generateDocument(Map arg) {
        // We need an Odisee XML request as a DOM
        if (!arg.xml) {
            throw new OdiseeException('There is no XML request!')
        }
        // Generate unique request ID
        arg.uniqueRequestId = UUID.randomUUID()
        // Set request directory
        //arg.requestDir = new File("${arg.principal.name}/${OdiseePath.DOCUMENT_DIR}", arg.uniqueRequestId.toString())
        arg.requestDir = new File("${ODISEE_USER}/${arg.principal.name}/${S_DOCUMENT}", arg.uniqueRequestId.toString())
        arg.requestDir.mkdirs()
        // Save XML request
        storageService.saveRequestToDisk(arg, OdiseeConstant.MINUS_ONE)
        // The result document(s)
        arg.document = []
        try {
            // Process request(s)
            use(DOMCategory) {
                arg.xml.request.eachWithIndex { request, i ->
                    // TODO Process requests asnychronously
                    // Remember which request should be processed now
                    arg.activeIndex = i
                    if (i > 0) {
                        // Reset all request-specific map keys
                        [
                                S_ID, OdiseeConstant.S_TEMPLATE, S_REVISION,
                                'documentName',
                                'templateDir', 'documentDir',
                                'templateFile'
                        ].each {
                            arg.remove(it)
                        }
                    }
                    //
                    prepareArg(arg)
                    // Save template to disk
                    storageService.saveTemplate(arg)
                    // Check values for template
                    checkPaths(arg)
                    // Process request and result
                    processSingleRequest(arg)
                    processSingleResult(arg)
                }
            }
            // TODO Post-process all requests
            postProcessAll(arg)
        } catch (e) {
            log.error 'ODI-xxxx: Exception occured during request processing', e
        } finally {
            // Clean up working directory
            // TODO Decouple this action from request processing (through a message queue), so this method is not called with every request and does not increase latency
            cleanupWorkingDirectory(arg)
        }
        // Return result
        if (log.debugEnabled) {
            log.debug "ODI-xxxx: Generated ${arg.document?.size() ?: 0} document(s)"
        }
        // arg == Map, document == List<OooDocument>
        [arg: arg, document: arg.document]
    }

}
