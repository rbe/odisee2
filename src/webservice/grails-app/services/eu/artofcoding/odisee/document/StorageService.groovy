/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import com.sun.org.apache.xerces.internal.dom.DeferredNode
import eu.artofcoding.grails.helper.FileHelper
import eu.artofcoding.grails.helper.XmlHelper
import eu.artofcoding.odisee.OdiseeException
import groovy.xml.XmlUtil

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static eu.artofcoding.odisee.OdiseePath.ODISEE_USER
import static eu.artofcoding.odisee.server.OdiseeConstant.*

class StorageService {

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
     *
     * @param filename
     * @return
     */
    private String getName(String filename) {
        String[] s = filename.split('\\.')
        s[0..s.length - 2].join(S_DOT)
    }

    /**
     * Read a file and save its content.
     */
    private Object fromFile(path) {
        Path f = path instanceof Path ? path : Paths.get(path)
        if (Files.exists(f) && Files.isReadable(f)) {
            f.readBytes()
        } else {
            log.error "ODI-xxxx: Can't find or read file ${path}"
            null
        }
    }

    /**
     * Create a document with 'name' from path. If the document does not exist, revision 1 is created
     * otherwise the revision is incremented by 1.
     * @param arg A map
     * @return Instance of domain class OooDocument
     */
    OooDocument createDocument(Map arg) {
        if (log.traceEnabled) {
            log.trace "ODI-xxxx: createDocument(${arg.inspect()})"
        }
        // Got filename?
        if (!arg.filename) {
            if (arg.data instanceof Path) {
                Path p = (Path) arg.data
                arg.filename = p.fileName.toString()
            } else if (arg.data instanceof String) {
                arg.data = Paths.get(arg.data)
                arg.filename = arg.data.name
            } else {
                arg.filename = 'unknown'
            }
        }
        // Load data depending on type?
        if (!arg.bytes) {
            if (arg.data instanceof String || arg.data instanceof Path) {
                arg.bytes = fromFile(arg.data)
            } else if (arg.data instanceof InputStream) {
                arg.bytes = arg.data.readBytes()
            } else if (arg.data instanceof byte[]) {
                arg.bytes = arg.data
            }
        }
        // Create new OooDocument
        OooDocument document = null
        if (arg.bytes) {
            document = new OooDocument()
            // Set names
            document.name = getName(arg.filename)
            document.filename = arg.filename
            // Instance of...
            document.instanceOfName = arg.instanceOf?.name
            document.instanceOfRevision = arg.instanceOf?.revision?.toLong()
            document.odiseeRequest = arg.odiseeRequest
            //document.data = Hibernate.createBlob(arg.bytes)
            document.bytes = arg.bytes
        } else {
            log.error "ODI-xxxx: createDocument(${arg.inspect()}): Failed to create instance of OOoDocument, no data: ${arg}"
        }
        document
    }

    /**
     * Remove a document.
     * @param arg
     */
    void remove(Map arg) {
        OooDocument.findById(arg.id)?.delete()
    }

    /**
     * Save XML request to file.
     * @param arg
     * @param requestNumber Request# to work with, -1 is the whole file.
     * @return File Reference to generated XML file.
     */
    Path saveRequestToDisk(Map arg, int requestNumber) {
        Path requestXMLFile = null
        String xmlString = null
        // Make XML string
        if (requestNumber == MINUS_ONE) {
            Path requestDir = (Path) arg.requestDir
            requestXMLFile = requestDir.resolve("${arg.uniqueRequestId}.xml" as String)
            xmlString = XmlUtil.serialize(arg.xml)
        } else {
            // Just save active request including <odisee> element
            String filename = String.format('%s_%04d.xml', arg.uniqueRequestId, requestNumber)
            Path documentDir = (Path) arg.documentDir
            requestXMLFile = documentDir.resolve(filename as String)
            DeferredNode deferredNode = (DeferredNode) arg.xml.request[requestNumber]
            xmlString = XmlHelper.asString(deferredNode)
        }
        // Write XML file
        Files.createDirectories(requestXMLFile.parent)
        FileHelper.writeUTF8(requestXMLFile, xmlString)
        requestXMLFile
    }

    /**
     * Find template in template directory or get it from storage service
     * and save it to disk (needed for OOo/LO).
     * @param arg
     */
    void saveTemplate(Map arg) {
        // Set document directory (same as request's working directory)
        arg.documentDir = arg.requestDir
        // Set template directory (same as request's working directory)
        arg.templateDir = Paths.get("${ODISEE_USER}/${arg.principal.name}", S_TEMPLATE)
        // Template
        arg.revision = 1
        // Check local template directory for latest revision of template
        Path localTemplate = arg.templateDir.resolve("${arg.template}.ott")
        if (!Files.exists(localTemplate)) {
            localTemplate = arg.templateDir.resolve("${arg.template}_rev${arg.revision}.ott")
        }
        boolean templateExists = Files.exists(localTemplate)
        if (log.isDebugEnabled()) {
            log.debug "ODI-xxxx: ${arg.principal.name} tries to access template ${localTemplate}, exists=${templateExists}"
        }
        if (templateExists) {
            // Point template for this request to local template
            arg.templateFile = localTemplate
        } else {
            throw new OdiseeException("ODI-xxxx: Template '${arg.template}' does not exist")
        }
    }

}
