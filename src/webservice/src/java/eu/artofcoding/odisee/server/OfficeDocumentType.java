/*
 * Odisee(R)
 * odisee-server, odisee-xml-processor
 *
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com/
 * Copyright (C) 2011-2012 art of coding UG, http://www.art-of-coding.eu/
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 02.08.12 18:32
 */

package eu.artofcoding.odisee.server;

public enum OfficeDocumentType {

    TEXT("Text document", "swriter", "ott", "odt", "writer_pdf_Export"),
    SPREADSHEET("Spreadsheet", "scalc", "ots", "ots", "calc_pdf_Export");

    private String description;
    private String internalType;
    private String pdfExportFilter;
    private String templateExtension;
    private String documentExtension;

    OfficeDocumentType(String description, String internalType, String templateExtension, String documentExtension, String pdfExportFilter) {
        this.description = description;
        this.internalType = internalType;
        this.templateExtension = templateExtension;
        this.documentExtension = documentExtension;
        this.pdfExportFilter = pdfExportFilter;
    }

    public String getDescription() {
        return description;
    }

    public String getInternalType() {
        return internalType;
    }

    public String getTemplateExtension() {
        return templateExtension;
    }

    public String getDocumentExtension() {
        return documentExtension;
    }

    public String getPdfExportFilter() {
        return pdfExportFilter;
    }

    /**
     * Find an enum constant by a search term.
     * @param what Can be description, internalType of pdfExportFilter.
     * @return OfficeDocumentType
     */
    public static OfficeDocumentType find(String what) {
        OfficeDocumentType r = null;
        for (OfficeDocumentType t : OfficeDocumentType.values()) {
            if (t.getDescription().equals(what) || t.getInternalType().equals(what)
                    || t.getTemplateExtension().equals(what) || t.getDocumentExtension().equals(what)
                    || t.getPdfExportFilter().equals(what)) {
                r = t;
                break;
            }
        }
        return r;
    }

}
