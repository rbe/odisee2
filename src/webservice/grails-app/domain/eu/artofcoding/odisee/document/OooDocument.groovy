/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import java.sql.Blob
import java.sql.Clob

/**
 * A document.
 */
class OooDocument {

    /**
     * Date of creation.
     */
    Date dateCreated

    /**
     * Date of last update.
     */
    Date lastUpdated

    /**
     * Name of this document.
     */
    String name

    /**
     * Is this a template?
     */
    Boolean template

    /**
     * Revision of document.
     */
    Long revision

    /**
     * This document is an instance of another -- a template.
     */
    String instanceOfName

    /**
     * This document is an instance of a certain revision of another document -- a template.
     */
    Long instanceOfRevision

    /**
     * The type.
     */
    MimeType mimeType

    /**
     * Original filename.
     */
    String filename

    /**
     * The extension (automatically extracted from filename).
     */
    String extension

    /**
     * The Odisee request.
     */
    String odiseeRequest
    Clob odiseeXmlRequest

    /**
     * The binary data.
     */
    byte[] bytes
    Blob data

    /**
     * Size of data.
     */
    int size

    def beforeInsert = {
        check()
    }

    def beforeUpdate = {
        throw new IllegalStateException('Updates prohibited')
    }

    private void check() {
        // Revision
        if (!revision) {
            revision = 1
        }
        // Set name
        if (!name && filename) {
            name = filename
        }
        // Extension
        try {
            extension = filename?.split("\\.").toList().last() // TODO Throw error when there's no extension?
        } catch (e) {
            // ignore
        }
        // Check if it's a template
        template = false
        // Is a certain string part of the mime type?
        ["template"].each {
            if (mimeType?.name?.indexOf(it) > -1 || mimeType?.browser?.indexOf(it) > -1) {
                template = true
                return
            }
        }
        /* TODO Create CLOB from odiseeRequest
        if (odiseeRequest) {
            odiseeXmlRequest = Hibernate.createClob(odiseeRequest)
        }
        */
        // Convert bytes property to BLOB
        if (bytes) {
            size = bytes.length
            // TODO data = Hibernate.createBlob(bytes)
        }
    }

    def getBytes() {
        bytes
    }
    
    def getSize() {
        size
    }
    
    /**
     * TODO: java.lang.UnsupportedOperationException: Blob may not be manipulated from creating session
     */
    def toByteArray() {
        data?.getBytes(1L, data?.length().toInteger())
    }

    /**
     * Transient properties.
     */
    static transients = [
        'odiseeRequest',
        'bytes'
    ]

    /**
     * Constraints.
     */
    static constraints = {
        dateCreated(nullable: true, editable: false)
        lastUpdated(nullable: true, editable: false)
        name(nullable: true)
        template(nullable: true)
        revision(nullable: true, editable: false)
        instanceOfName(nullable: true, editable: false)
        instanceOfRevision(nullable: true, editable: false)
        filename(nullable: true, editable: false)
        extension(nullable: true, editable: false)
        mimeType(nullable: true)
        odiseeXmlRequest(nullable: true, editable: false)
        data(nullable: true)
    }

    /**
     * Mapping.
     */
    static mapping = {
        table 'T2_DOC'
    }

}
