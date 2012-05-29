/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.flux.grails.helper

import org.hibernate.Hibernate

/**
 * Helper for Hibernate.
 */
class HibernateHelper {

    /**
     * Convert a byte array into a java.sql.Blob.
     * @return java.sql.Blob
     */
    static def toBlob(byteArray) {
        Hibernate.createBlob(byteArray)
    }

    /**
     * Convert a java.sql.Blob property into a byte array.
     * @return byte[]
     */
    static def toByteArray(prop) {
        prop?.getBytes(1L, prop.length().toInteger())
    }

}
