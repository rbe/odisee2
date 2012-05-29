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
 * Odisee instance configuration.
 */
class OdiseeInstance {

    /**
     * Date of creation.
     */
    Date dateCreated

    /**
     * Date of last update.
     */
    Date lastUpdated

    /**
     * Name of an instance.
     */
    String name

    /**
     * Group this instance belongs to.
     */
    OdiseeGroup group

    /**
     * Host/IP and port.
     */
    String host
    Integer port

    /**
     * Belongs to...
     */
    static belongsTo = [
            group: OdiseeGroup
    ]

    /**
     * Has-many relationships.
     */
    static hasMany = [
            // Command line options for starting OpenOffice process.
            cmdOption: OdiseeCmdOption
    ]

    /**
     * Constraints.
     */
    static constraints = {
        name blank: false, nullable: false
        host blank: false, nullable: false
        port nullable: false
        cmdOption inList: OdiseeCmdOption.list()
    }

}
