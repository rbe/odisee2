/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.NameHelper
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseeXmlCategory
import eu.artofcoding.odisee.server.OfficeConnectionFactory
import groovy.xml.dom.DOMCategory
import org.springframework.beans.factory.InitializingBean

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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
            Path odiinst = ODISEE_HOME.resolve(S_ETC_ODIINST)
            // Read contents of odiinst
            // odiinst=[odi1, 127.0.0.1, 2001, /Applications/OpenOffice.org.app, /Users/rbe/project/odisee/var/user/odi1, nologo nofirststartwizard norestart nodefault nolockcheck nocrashreport, true]
            List<String> contents = []
            if (Files.exists(odiinst)) {
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
                contents << ['odi1', '127.0.0.1', '2001', '', '', '', 'false']
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
                arg.revision = 1 //storageService.getDocumentsLatestRevision(name: arg.template)
                // Save in XML request for Odisee
                template.setAttribute(S_REVISION, arg.revision.toString())
            }
        }
        log.debug "arg=${arg}"
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
            template.setAttribute('path', arg.templateFile.toAbsolutePath().toString())
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
        Path requestXMLFile = storageService.saveRequestToDisk(arg, arg.activeIndex)
        // Process request
        use(OdiseeXmlCategory) {
            def request = arg.xml.request[arg.activeIndex]
            arg.result = requestXMLFile.toDocument(officeConnectionFactory, 0) // requestNumber = 0 as file contains only one request
            if (!arg.result) {
                // This should never happen; except all OOo instances have crashed.
                String group = 'group0'
                log.error "ODI-xxxx: ${requestXMLFile.fileName.toString()}/${arg.activeIndex}: Got no result, maybe all OpenOffice instance(s) in group '${group}' are unwilling to perform?"
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
        arg.requestDir = Paths.get("${ODISEE_USER}/${arg.principal.name}/${S_DOCUMENT}", arg.uniqueRequestId.toString())
        Files.createDirectories(arg.requestDir)
        // Save XML request
        storageService.saveRequestToDisk(arg, MINUS_ONE)
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
                                S_ID, S_TEMPLATE, S_REVISION,
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
        }
        // Return result
        if (log.debugEnabled) {
            log.debug "ODI-xxxx: Generated ${arg.document?.size() ?: 0} document(s)"
        }
        // arg == Map, document == List<OooDocument>
        [arg: arg, document: arg.document]
    }

}
