/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import org.apache.pdfbox.util.PDFMergerUtility

import java.nio.file.Path

/**
 * Provide services for dealing with PDF files.
 */
class PdfService {

    /**
     * Merge multiple PDF files into a single one.
     * @param target
     * @param pdfFiles
     * @return
     */
    Path mergeDocuments(Path target, List<Path> pdfFiles) {
        PDFMergerUtility merger = new PDFMergerUtility()
        try {
            pdfFiles.each { Path p ->
                merger.addSource(p.toFile())
            }
            merger.destinationFileName = target.toAbsolutePath().toString()
            merger.mergeDocuments()
            target
        } catch (e) {
            log.error "Could not merge ${pdfFiles.join(', ')} into ${target}", e
            null
        }
    }

}
