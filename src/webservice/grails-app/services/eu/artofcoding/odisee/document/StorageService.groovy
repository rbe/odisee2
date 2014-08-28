/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
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

import static eu.artofcoding.odisee.OdiseePath.ODISEE_USER
import static eu.artofcoding.odisee.server.OdiseeConstant.*
/**
 *
 */
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
        File f = path instanceof File ? path : new File(path)
        if (f.exists() && f.canRead()) {
            f.readBytes()
        } else {
            log.error "ODI-xxxx: Can't find or read file ${path}"
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
            if (arg.data instanceof File) {
                arg.filename = arg.data.name
            } else if (arg.data instanceof String) {
                arg.data = new File(arg.data)
                arg.filename = arg.data.name
            } else {
                arg.filename = 'unknown'
            }
        }
        // Load data depending on type?
        if (!arg.bytes) {
            if (arg.data instanceof String || arg.data instanceof File) {
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
    File saveRequestToDisk(Map arg, int requestNumber) {
        File requestXMLFile = null
        String xmlString = null
        // Make XML string
        if (requestNumber == MINUS_ONE) {
            requestXMLFile = new File(arg.requestDir as File, "${arg.uniqueRequestId}.xml" as String)
            xmlString = XmlUtil.serialize(arg.xml)
        } else {
            // Just save active request including <odisee> element
            String filename = String.format('%s_%04d.xml', arg.uniqueRequestId, requestNumber)
            requestXMLFile = new File(arg.documentDir as File, filename as String)
            DeferredNode deferredNode = (DeferredNode) arg.xml.request[requestNumber]
            xmlString = XmlHelper.asString(deferredNode)
        }
        // Write XML file
        if (requestXMLFile.exists()) {
            throw new OdiseeException("ODI-xxxx: Cannot write request XML file ${requestXMLFile.absolutePath} as it exists already")
        } else {
            requestXMLFile.parentFile.mkdirs()
            FileHelper.writeUTF8(requestXMLFile, xmlString)
        }
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
        arg.templateDir = new File("${ODISEE_USER}/${arg.principal.name}", S_TEMPLATE)
        // Template
        arg.revision = 1
        // Check local template directory for latest revision of template
        File localTemplate = new File(arg.templateDir as File, "${arg.template}.ott")
        if (!localTemplate.exists()) {
            localTemplate = new File(arg.templateDir as File, "${arg.template}_rev${arg.revision}.ott")
        }
        boolean templateExists = localTemplate.exists()
        if (log.isDebugEnabled()) {
            log.debug "ODI-xxx: ${arg.principal.name} tries to access template ${localTemplate}, exists=${templateExists}"
        }
        if (templateExists) {
            // Point template for this request to local template
            arg.templateFile = localTemplate
        }
        // Check
        if (!arg.templateFile || !arg.templateFile?.exists()) {
            throw new OdiseeException("ODI-xxxx: Template ${arg.templateFile} does not exist")
        }
    }

}
