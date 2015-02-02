/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import grails.transaction.Transactional
import groovy.xml.dom.DOMCategory
import org.w3c.dom.Element

import java.nio.file.Files
import java.nio.file.Paths
import java.security.Principal

import static eu.artofcoding.odisee.OdiseePath.ODISEE_USER
import static eu.artofcoding.odisee.server.OdiseeConstant.*

@Transactional
class OdiseeService {

    /**
     * The scope. See http://www.grails.org/Services.
     * prototype request flash flow conversation session singleton
     */
    def scope = 'prototype'

    TemplateService templateService

    RequestService requestService

    StorageService storageService

    PostProcessService postProcessService

    private static void resetRequest(final Map arg) {
        [
                S_ID, S_TEMPLATE, S_REVISION,
                'documentName',
                'templateDir', 'templateFile',
                'documentDir',
                'document', 'data', 'bytes', 'filename'
        ].each {
            arg.remove(it)
        }
    }

    /**
     * Generate a document using document service and OOo service.
     * @param arg Map: xml: an XML request (see request.xsd in Odisee).
     * @return List with generated OooDocument instance(s).
     */
    List<Document> generateDocument(final Principal principal, final Element xml) {
        final Map arg = [
                principal      : principal,
                xml            : xml,
                activeIndex    : -1,
                uniqueRequestId: UUID.randomUUID(),
                requestDir     : null,
                odiseeRequest  : null,
                documentName   : null,
                templateDir    : null,
                documentDir    : null,
                templateFile   : null,
                id             : null,
                template       : null,
                revision       : 1,
                document       : [],
                data           : null,
                bytes          : null,
                filename       : null,
                result         : []
        ]
        final GString userDocumentDir = "${ODISEE_USER}/${arg.principal.name}/${S_DOCUMENT}"
        arg.requestDir = Paths.get(userDocumentDir, arg.uniqueRequestId.toString())
        Files.createDirectories(arg.requestDir)
        requestService.extractRequestAndSaveToDisk(arg, MINUS_ONE)
        use(DOMCategory) {
            arg.xml.request.eachWithIndex { request, i ->
                arg.activeIndex = i
                if (i > 0) {
                    resetRequest(arg)
                }
                templateService.extractTemplateFromRequest(arg)
                templateService.copyTemplateToRequest(arg)
                templateService.checkPaths(arg)
                requestService.processSingleRequest(arg)
                postProcessService.postProcessRequest(arg)
                arg.result.output.each { file ->
                    arg.document << storageService.createDocument(data: file)
                }
            }
        }
        postProcessService.postProcessOdisee(arg)
        if (log.debugEnabled) {
            log.debug "ODI-xxxx: Generated ${arg.document?.size() ?: 0} document(s)"
        }
        arg.document
    }

}
