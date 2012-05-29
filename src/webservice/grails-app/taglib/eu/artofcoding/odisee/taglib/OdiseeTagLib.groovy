/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.taglib

/**
 *
 */
class OdiseeTagLib {

    /**
     * Our namespace.
     */
    static namespace = "odisee"

    /**
     * Which tags return objects?
     */
    static returnObjectForTags = []

    /**
     * Create link to generate a document using OOo service.
     * Mandatory attributes:
     * attr.id
     * attr.template
     * attr.doctype
     *
     * Value attribute: a map with key = OOo field, value: [value: , postSetMacro: ]
     * attr.value, e.g.: [afield: [value: 'the value', postSetMacro: 'vnd...']]
     *
     * Optional attributes:
     * attr.revision: the revision of the template, if not given the latest is used
     * attr.streamtype
     */
    def generate = { attr, body ->
        def arg = [:]
        arg.putAll(
                attr.findAll { k, v ->
                    !(k in ["controller", "action"])
                }
        )
        out << g.link(controller: 'document', action: 'generate', params: arg) { body() }
    }

    /**
     * Create a link to stream a document.
     * attr.id
     * attr.doctype | streamtype
     * attr.
     */
    def stream = { attr, body ->
        def arg = [:]
        arg.putAll(
                attr.findAll { k, v ->
                    !(k in ["controller", "action"])
                }
        )
        out << g.link(controller: arg.controller ?: 'document', action: 'stream', params: arg) { body() }
    }

    /**
     *
     */
    def remove = { attr, body ->
        def arg = [:]
        arg.putAll(
                attr.findAll { k, v ->
                    !(k in ["controller", "action"])
                }
        )
        out << g.link(controller: arg.controller ?: 'document', action: 'remove', params: arg) { body() }
    }
}
