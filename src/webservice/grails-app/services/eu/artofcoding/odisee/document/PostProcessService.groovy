/*
 * Odisee(R)
 *
 * Copyright (C) 2011-2015 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 14.09.14 12:59
 */
package eu.artofcoding.odisee.document

import eu.artofcoding.grails.helper.FileHelper
import eu.artofcoding.grails.helper.NameHelper
import groovy.xml.dom.DOMCategory
import org.w3c.dom.Element

import java.nio.file.Path
import java.nio.file.Paths

import static eu.artofcoding.odisee.OdiseePath.ODISEE_USER
import static eu.artofcoding.odisee.server.OdiseeConstant.S_PDF

class PostProcessService {

    static transactional = false

    /**
     * The storage service.
     */
    StorageService storageService

    /**
     * The PDF service.
     */
    PdfService pdfService

    /**
     * <action type="merge-with">
     */
    void processMergeWith(Map arg, Element actionElement) {
        def request = null
        // What file was generated by this request?
        String generatedFile = arg.result.output[0]
        if (generatedFile) {
            // Create new filename
            Map decomposedFilename = FileHelper.decomposeFilename(generatedFile)
            Path destPdfFile = Paths.get("${decomposedFilename.name}_merged.pdf")
            List<Path> pdfFiles = []
            // Get order to merge PDF files from XML request
            use(DOMCategory) {
                request = arg.xml.request[arg.activeIndex]
                // Build ordered list with PDF files to merge
                actionElement.'*'.each {
                    // <result-placeholder/>
                    // Where to place the generated PDF file?
                    if (it.name() == 'result-placeholder') {
                        pdfFiles << Paths.get(generatedFile)
                    } else {
                        String filename = it.'@file'.toString()
                        pdfFiles << Paths.get("${ODISEE_USER}/${arg.principal.name}/${S_PDF}", filename)
                    }
                }
            }
            // Merge PDFs and generate a new one
            Path mergedPdf = pdfService.mergeDocuments(destPdfFile, pdfFiles)
            arg.document << storageService.createDocument(data: mergedPdf)
        }
    }

    /**
     * <action type="merge-results">
     */
    void processMergeResults(Map arg, Element actionElement) {
        // What file was generated by this request?
        String generatedFile = arg.result.output[0]
        // Create new filename
        Map decomposedFilename = FileHelper.decomposeFilename(generatedFile)
        Path destPdfFile = Paths.get("${decomposedFilename.name}_merged.pdf")
        List<Path> pdfFiles = []
        // Get order to merge PDF files from XML request
        use(DOMCategory) {
            arg.document.each { d ->
                pdfFiles << Paths.get(arg.documentDir as String, d.filename as String)
            }
        }
        // Merge PDFs and generate a new one
        Path mergedPdf = pdfService.mergeDocuments(destPdfFile, pdfFiles)
        arg.document << storageService.createDocument(data: mergedPdf)
    }

    void processActions(Map arg, actions) {
        use(DOMCategory) {
            String methodName = null
            actions.eachWithIndex { action, idx ->
                methodName = action.'@type'[0].toUpperCase() + NameHelper.mapDashToCamelCase(action.'@type')[1..-1]
                "process${methodName}"(arg, action)
            }
        }
    }

    void postProcessRequest(Map arg) {
        use(DOMCategory) {
            def postProcess = arg.xml.request[arg.activeIndex].'post-process'
            String methodName = null
            postProcess.'*'.eachWithIndex { action, idx ->
                methodName = action.'@type'[0].toUpperCase() + NameHelper.mapDashToCamelCase(action.'@type')[1..-1]
                "process${methodName}"(arg, action)
            }
        }
    }

    void postProcessOdisee(Map arg) {
        use(DOMCategory) {
            def postProcess = arg.xml.'post-process'
            String methodName = null
            postProcess.'*'.eachWithIndex { action, idx ->
                methodName = action.'@type'[0].toUpperCase() + NameHelper.mapDashToCamelCase(action.'@type')[1..-1]
                "process${methodName}"(arg, action)
            }
        }
    }

}
