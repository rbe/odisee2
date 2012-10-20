/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.helper

/**
 *
 */
class OdiseeConstant {

    private static final int THOUSAND = 1000
    public static final int TIMEOUT_1SEC = 1 * THOUSAND
    public static final int UNUSABLE_TIMEOUT = 30 * THOUSAND
    public static final int SPIN_TIMEOUT = 1 * THOUSAND

    public static final String S_UTF8 = 'UTF-8'
    public static final String S_TRUE = 'true'
    public static final String S_FALSE = 'false'
    public static final String S_GROUP0 = 'group0'
    public static final String S_UNDERSCORE = '_'
    public static final String S_LATEST = 'LATEST'
    public static final String S_ODISEE_HOME = 'ODISEE_HOME'
    public static final String S_ODISEE_DEPLOY = 'ODISEE_DEPLOY'
    public static final String S_ODISEE_TMP = 'ODISEE_TMP'
    public static final String S_VAR = 'var'
    public static final String S_VAR_TMP = 'var/tmp'
    public static final String S_TEMPLATE = 'template'
    public static final String S_DOCUMENT = 'document'
    public static final String WRITER_EXT_REGEX = /.o\wt$/
    public static final String REVISION_REGEX = /rev\d+/

    /**
     * Mime types. See http://de.selfhtml.org/diverses/mimetypen.htm.
     */
    static final List<Map<String, String>> MIME_TYPES = [
            [name: 'Unknown', extension: '*', browser: 'application/octet-stream'],
            [name: 'OpenOffice.org Writer (odt)', extension: 'odt', browser: 'application/vnd.oasis.opendocument.text'],
            [name: 'OpenOffice.org Writer Template (ott)', extension: 'ott', browser: 'application/vnd.oasis.opendocument.text-template'],
            [name: 'OpenOffice.org Writer Web (oth)', extension: 'oth', browser: 'application/vnd.oasis.opendocument.text-web'],
            [name: 'OpenOffice.org Writer Masterdocument (odm)', extension: 'odm', browser: 'application/vnd.oasis.opendocument.text-master'],
            [name: 'OpenOffice.org Draw (odg)', extension: 'odg', browser: 'application/vnd.oasis.opendocument.graphics'],
            [name: 'OpenOffice.org Draw Template (otg)', extension: 'otg', browser: 'application/vnd.oasis.opendocument.graphics-template'],
            [name: 'OpenOffice.org Impress (odp)', extension: 'odp', browser: 'application/vnd.oasis.opendocument.presentation'],
            [name: 'OpenOffice.org Impress Template (otp)', extension: 'otp', browser: 'application/vnd.oasis.opendocument.presentation-template'],
            [name: 'OpenOffice.org Calc (ods)', extension: 'ods', browser: 'application/vnd.oasis.opendocument.spreadsheet'],
            [name: 'OpenOffice.org Calc Template (ots)', extension: 'ots', browser: 'application/vnd.oasis.opendocument.spreadsheet-template'],
            [name: 'OpenOffice.org Chart (odc)', extension: 'odc', browser: 'application/vnd.oasis.opendocument.chart'],
            [name: 'OpenOffice.org Formula (odf)', extension: 'odf', browser: 'application/vnd.oasis.opendocument.formula'],
            [name: 'OpenOffice.org Image (odi)', extension: 'odi', browser: 'application/vnd.oasis.opendocument.image'],
            [name: 'JPEG (jpg)', extension: 'jpg', browser: 'image/jpeg'],
            [name: 'JPEG (jpeg)', extension: 'jpeg', browser: 'image/jpeg'],
            [name: 'TIFF (tif)', extension: 'tif', browser: 'image/tif'],
            [name: 'TIFF (tiff)', extension: 'tiff', browser: 'image/tif'],
            [name: 'Portable Network Graphics (png)', extension: 'png', browser: 'image/png'],
            [name: 'Portable Document Format (pdf)', extension: 'pdf', browser: 'application/pdf'],
            [name: 'Rich Text Format (rtf)', extension: 'rtf', browser: 'application/rtf'],
            [name: 'Microsoft Office Word 97-2003 (doc)', extension: 'doc', browser: 'application/msword'],
            [name: 'Microsoft Office Word 97-2003 Template (dot)', extension: 'dot', browser: 'application/msword'],
            [name: 'Microsoft Office Excel 97-2003 (xls)', extension: 'xls', browser: 'application/vnd.ms-excel'],
            [name: 'Microsoft Office Excel 97-2003 Template (xlt)', extension: 'xlt', browser: 'application/vnd.ms-excel'],
            [name: 'Microsoft Office Excel 97-2003 Addin (xla)', extension: 'xla', browser: 'application/vnd.ms-excel'],
            [name: 'Microsoft Office PowerPoint 97-2003 (ppt)', extension: 'ppt', browser: 'application/vnd.ms-powerpoint'],
            [name: 'Microsoft Office PowerPoint 97-2003 Template (pot)', extension: 'pot', browser: 'application/vnd.ms-powerpoint'],
            [name: 'Microsoft Office PowerPoint 97-2003 (pps)', extension: 'pps', browser: 'application/vnd.ms-powerpoint'],
            [name: 'Microsoft Office PowerPoint 97-2003 (ppa)', extension: 'ppa', browser: 'application/vnd.ms-powerpoint'],
            [name: 'Microsoft Office Word 2007 (docx)', extension: 'docx', browser: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
            [name: 'Microsoft Office Word 2007 Template (dotx)', extension: 'dotx', browser: 'application/vnd.openxmlformats-officedocument.wordprocessingml.template'],
            [name: 'Microsoft Office Word 2007 (docm)', extension: 'docm', browser: 'application/vnd.ms-word.document.macroEnabled.12'],
            [name: 'Microsoft Office Word 2007 Template (dotm)', extension: 'dotm', browser: 'application/vnd.ms-word.template.macroEnabled.12'],
            [name: 'Microsoft Office Excel 2007 (xlsx)', extension: 'xlsx', browser: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'],
            [name: 'Microsoft Office Excel 2007 Template (xltx)', extension: 'xltx', browser: 'application/vnd.openxmlformats-officedocument.spreadsheetml.template'],
            [name: 'Microsoft Office Excel 2007 (xlsm)', extension: 'xlsm', browser: 'application/vnd.ms-excel.sheet.macroEnabled.12'],
            [name: 'Microsoft Office Excel 2007 Template (xltm)', extension: 'xltm', browser: 'application/vnd.ms-excel.template.macroEnabled.12'],
            [name: 'Microsoft Office Excel 2007 Addin (xlam)', extension: 'xlam', browser: 'application/vnd.ms-excel.addin.macroEnabled.12'],
            [name: 'Microsoft Office Excel 2007 Binary (xlsb)', extension: 'xlsb', browser: 'application/vnd.ms-excel.sheet.binary.macroEnabled.12'],
            [name: 'Microsoft Office PowerPoint 2007 (pptx)', extension: 'pptx', browser: 'application/vnd.openxmlformats-officedocument.presentationml.presentation'],
            [name: 'Microsoft Office PowerPoint 2007 Template (potx)', extension: 'potx', browser: 'application/vnd.openxmlformats-officedocument.presentationml.template'],
            [name: 'Microsoft Office PowerPoint 2007 (ppsx)', extension: 'ppsx', browser: 'application/vnd.openxmlformats-officedocument.presentationml.slideshow'],
            [name: 'Microsoft Office PowerPoint 2007 Addin (ppam)', extension: 'ppam', browser: 'application/vnd.ms-powerpoint.addin.macroEnabled.12'],
            [name: 'Microsoft Office PowerPoint 2007 (pptm)', extension: 'pptm', browser: 'application/vnd.ms-powerpoint.presentation.macroEnabled.12'],
            [name: 'Microsoft Office PowerPoint 2007 Template (potm)', extension: 'potm', browser: 'application/vnd.ms-powerpoint.template.macroEnabled.12'],
            [name: 'Microsoft Office PowerPoint 2007 Slideshow (ppsm)', extension: 'ppsm', browser: 'application/vnd.ms-powerpoint.slideshow.macroEnabled.12']
    ]

}
