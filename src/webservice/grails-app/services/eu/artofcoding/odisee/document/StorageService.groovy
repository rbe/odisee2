/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.flux.grails.helper.FileHelper
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseePath
import eu.artofcoding.odisee.OdiseeWebserviceConstant
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import eu.artofcoding.flux.grails.helper.XmlHelper
import com.sun.org.apache.xerces.internal.dom.DeferredNode

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
     * Paths constants.
     */
    private static final OdiseePath ODISEE_PATH = OdiseePath.instance

    /**
     *
     * @param filename
     * @return
     */
    private String getName(filename) {
        String[] s = filename.split('\\.')
        s[0..s.length - 2].join(OdiseeWebserviceConstant.S_DOT)
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
     * Determine mime type.
     * @param arg
     * @return
     */
    MimeType getMimeType(Map arg) {
        MimeType mimeType = null
        Map decomposedFilename = FileHelper.decomposeFilename(arg.name)
        // Filename with extension?
        if (decomposedFilename.ext) {
            mimeType = MimeType.findByExtension(decomposedFilename.ext)
        }
        /*
        else {
            // See GRAILS-1186, findByNameOrExtensionOrBrowser
            mimeType = MimeType.findByExtensionOrBrowser(arg.name, arg.name)
        }
        */
        if (mimeType) {
            if (log.debugEnabled) {
                log.debug "ODI-xxxx: Mime type for ${arg.name} is ${mimeType.name}"
            }
        } else {
            mimeType = MimeType.findByName('Unknown') //"appliction/octet-stream"
            if (log.debugEnabled) {
                log.debug "ODI-xxxx: Could not recognize mime type of ${arg.name}; using application/octet-stream"
            }
        }
        mimeType
    }

    /**
     *
     * @param arg
     * @return
     */
    boolean hasDocument(Map arg) {
        /*
        if (log.traceEnabled) {
            log.trace "ODI-xxxx: hasDocument: arg=${arg}"
        }
        */
        List<Integer> doc = OooDocument.withCriteria {
            or {
                if (arg.name) {
                    ilike('name', arg.name)
                }
                if (arg.revision) {
                    eq('revision', arg.revision.toLong())
                }
                if (arg.mimeType) {
                    mimeType {
                        eq(OdiseeWebserviceConstant.S_NAME, arg.mimeType)
                    }
                }
            }
            projections {
                count('name')
            }
        }
        boolean b = false
        if (doc?.size() == 1) {
            b = true
        }
        b
    }

    /**
     * Add a document.
     * @param arg
     * @return
     */
    OooDocument addDocument(Map arg) {
        if (log.traceEnabled) {
            log.trace "ODI-xxxx: addDocument(${arg.inspect()})"
        }
        OooDocument document = createDocument(arg)
        if (document) {
            // Revision: lookup if we have a document with same name and filename
            List<OooDocument> existingTemplates = OooDocument.findAllByNameAndFilename(document.name, document.filename)
            int highestRevisionNumber = (int) existingTemplates.inject(0) { int o, OooDocument n ->
                n.revision > o ? n.revision : o
            }
            document.revision = highestRevisionNumber + 1
            // Save new document
            document.save(flush: true)
        }
        document
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
            // Mime type
            document.mimeType = getMimeType(name: document.filename)
            document.odiseeRequest = arg.odiseeRequest
            //document.data = Hibernate.createBlob(arg.bytes)
            document.bytes = arg.bytes
        } else {
            log.error "ODI-xxxx: createDocument(${arg.inspect()}): Failed to create instance of OOoDocument, no data: ${arg}"
        }
        document
    }

    /**
     * Get all revisions of a document.
     * @param arg
     * @return
     */
    List<Integer> getDocumentRevision(Map arg) {
        OooDocument.findAllByName(arg.name)*.revision
    }

    /**
     * Get latest revision for a document.
     * @param arg
     * @return
     */
    Integer getDocumentsLatestRevision(Map arg) {
        getDocumentByLatestRevision(name: arg.name)?.revision
    }

    /**
     * Get a document by name and optionally by revision (default is 1).
     * @param arg
     * @return
     */
    OooDocument getDocumentByRevision(Map arg) {
        OooDocument.findByNameAndRevision(arg.name, arg.revision ?: 1)
    }

    /**
     * Get a document with its latest revision.
     * @param arg
     * @return
     */
    OooDocument getDocumentByLatestRevision(Map arg) {
        try {
            List<OooDocument> list = OooDocument.findAllByName(arg.name, [sort: OdiseeWebserviceConstant.S_REVISION, order: 'desc'])
            if (list?.size() > 0) { // TODO Ask database for "latest" revision!?
                list.first()
            }
        } catch (e) { // NoSuchElementException: Cannot access last() from an empty List
            log.error e
        }
    }

    /**
     * Returns one or more document(s) queried by given map keys.
     * @param arg Map: name, revision, mimeType
     */
    List<OooDocument> getDocument(Map arg) {
        OooDocument.withCriteria {
            and {
                ilike(OdiseeWebserviceConstant.S_NAME, arg.name)
                eq(OdiseeWebserviceConstant.S_REVISION, arg.revision?.toLong() ?: getDocumentByLatestRevision(arg)?.revision)
                if (arg.mimeType) {
                    or {
                        mimeType {
                            ilike(OdiseeWebserviceConstant.S_NAME, "%${arg.mimeType}")
                        }
                        ilike('filename', "%${arg.mimeType}")
                    }
                }
            }
        }
    }

    /**
     * Get binary data from document by name and revision.
     * @param arg
     * @return
     */
    byte[] getDocumentData(Map arg) {
        getDocument(name: arg.name, revision: arg.revision)[0]?.toByteArray()
    }

    /**
     * Get binary data from document's latest revision.
     * @param arg
     * @return
     */
    byte[] getDocumentsLatestRevisionData(Map arg) {
        getDocumentByLatestRevision(name: arg.name).toByteArray()
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
        if (requestNumber == OdiseeWebserviceConstant.MINUS_ONE) {
            requestXMLFile = new File(arg.requestDir, "${arg.uniqueRequestId}.xml")
            xmlString = XmlUtil.serialize(arg.xml)
        } else {
            // Just save active request including <odisee> element
            //requestXMLFile = new File(arg.documentDir, "${arg.documentName}.xml")
            String filename = String.format('%s_%04d.xml', arg.uniqueRequestId, requestNumber)
            requestXMLFile = new File(arg.documentDir, filename)
            DeferredNode deferredNode = (DeferredNode) arg.xml.request[requestNumber]
            xmlString = XmlHelper.asString(deferredNode)
        }
        // Write XML file
        if (requestXMLFile.exists()) {
            throw new OdiseeException("ODI-xxxx: Cannot write request XML file ${requestXMLFile.absolutePath} as it exists already")
        } else {
            FileHelper.writeUTF8(requestXMLFile, xmlString)
        }
        requestXMLFile
    }

    /**
     * Find template in template directory or get it from storage service and save it to disk.
     * @param arg
     */
    void saveTemplate(Map arg) {
        // Set document directory (same as request's working directory)
        arg.documentDir = arg.requestDir
        // Set template directory (same as request's working directory)
        arg.templateDir = arg.requestDir
        // Template
        arg.templateFile = new File(arg.templateDir, "${arg.template}_rev${arg.revision}.ott")
        // Check local template directory for latest revision of template
        File localTemplate = new File(ODISEE_PATH.TEMPLATE_DIR, "${arg.template}_rev${arg.revision}.ott")
        if (localTemplate.exists()) {
            // Copy it to arg.templateDir
            arg.templateFile << localTemplate.bytes
        } else {
            // Get template from StorageService
            // Does the storage service has got the template?
            boolean hasTemplate = hasDocument(name: arg.template, revision: arg.revision)
            if (!hasTemplate) {
                throw new OdiseeException("Template ${arg.template} (revision ${arg.revision}) not found in template database!")
            }
            if (!arg.templateFile.exists()) {
                // Retrieve template via StorageService and save it to a file
                byte[] templateBytes = getDocumentData(name: arg.template, revision: arg.revision)
                if (templateBytes) {
                    arg.templateFile.createNewFile()
                    arg.templateFile << templateBytes
                    /*
                    arg.templateFile.withOutputStream { os ->
                        os.write(templateBytes, 0, templateBytes.length)
                    }
                    */
                    log.info "ODI-xxxx: Copied template ${arg.template} revision ${arg.revision} as ${arg.templateFile.absolutePath}"
                } else {
                    throw new OdiseeException("Cannot save template, template ${arg.template} with revision ${arg.revision} not found!")
                }
            }
            // Copy it to arg.templateDir
            localTemplate.createNewFile()
            localTemplate << arg.templateFile.bytes
        }
        // Check
        if (!arg.templateFile.exists()) {
            throw new OdiseeException("ODI-xxxx: Template ${arg.templateFile} does not exist")
        }
    }

}
