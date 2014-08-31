/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

/**
 * Grails' URL mappings.
 */
class UrlMappings {

    static mappings = {
        // Index
        '/'(view: 'index')
        // Generate document from template
        "/document/generate"(controller: 'document', action: 'generate')
        // Generate document from template
        "/document/generate/$id?"(controller: 'document', action: 'generate')
    }

}
