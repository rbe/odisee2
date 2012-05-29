/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.admin

/**
 * Odisee instance group.
 */
class OdiseeGroup {

    /**
     * Date of creation.
     */
    Date dateCreated

    /**
     * Date of last update.
     */
    Date lastUpdated

    /**
     * Name of this group.
     */
    String name

    /**
     * Has-many relationships.
     */
    static hasMany = [
            instance: OdiseeInstance
    ]

    /**
     * Constraints.
     */
    static constraints = {
        name blank: false, nullable: false
    }

}
