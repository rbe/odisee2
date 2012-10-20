/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

/**
 * Grails' URL mappings.
 */
class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }
        //'500'(view: '/error')
        // Index
        '/'(controller: 'document', action: 'index')
        // Add a template
        '/document/add'(controller: 'document', action: 'add')
        // Generate document from template
        "/document/generate/$id?"(controller: 'document', action: 'generate')
    }

}
