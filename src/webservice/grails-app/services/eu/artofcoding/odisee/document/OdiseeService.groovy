/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.XmlHelper

import org.w3c.dom.Element

/**
 * A service for Odisee.
 */
class OdiseeService {

    /**
     * The OpenOffice service.
     */
    OooService oooService

    /**
     * Generate one or more document(s) using OooService.
     * Used to generate a document from values of GET parameters.
     * @param arg A map
     *            name
     *            template
     *            prop
    List<OooDocument> generateWithProperties(arg) {
        // Generate map with OOo properties
        // UNUSED? def prop = oooService.mapToOooProps(map: arg)
        // Generate Odisee XML request
        def xml = new StreamingMarkupBuilder().bind {
            odisee {
                request(name: arg.name ?: arg.template, id: arg.prop.id.value) {
                    // Example for pre-save-macro: "Standard.oooservice.preSave?language=Basic&amp;location=document",
                    ooo(
                            // Use argument or set default values
                            group: arg.ooogroup ?: 'group0',
                            host: arg.ooohost ?: '127.0.0.1',
                            port: arg.oooport ?: 2002,
                            'pre-save-macro': arg.preSaveMacro,
                            outputPath: arg.outputPath ?: '',
                            outputFormat: arg.list('outputFormat') as String[]
                    )
                    template(name: arg.template, revision: arg.revision ?: 'LATEST')
                    archive(database: arg.archivedb ?: false, files: arg.archivefiles ?: false)
                    userfields {
                        arg.prop.each { k, v ->
                            userfield(name: k, 'post-set-macro': v.postSetMacro, v.value)
                        }
                    }
                }
            }
        }
        // Send reqeust to OooService
        generateWithXml(xml.toString())
    }
    */

    /**
     * Call OooService with a DOM-ified XML request.
     * @param arg May be a XmlSlurped document (via request.XML) or a String.
     * @return List List with generated documents.
     */
    List<OooDocument> generateWithXml(arg) {
        // Build Odisee XML request using StreamingMarkupBuilder
        Element documentElement = XmlHelper.asElement(arg)
        oooService.generateDocument(xml: documentElement)
    }

}
