/*
 * odisee
 * webservice
 * Copyright (C) 2011-2013 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 30.11.12 08:58
 */

package eu.artofcoding.odisee.server;

public class OdiseeConstant {

    public static final String NATIVE_TEMPLATE_REGEX = ".*\\.ot[gpst]".intern();

    public static final String NATIVE_DOCUMENT_REGEX = ".*\\.od[gpst]".intern();

    public static final String WRITER_EXT_REGEX = ".o\\wt$".intern();

    public static final String REVISION_REGEX = "rev\\d+".intern();

    public static final int THOUSAND = 1000;

    public static final int TIMEOUT_1SEC = 1 * THOUSAND;

    public static final int UNUSABLE_TIMEOUT = 30 * THOUSAND;

    public static final int SPIN_TIMEOUT = 1 * THOUSAND;

    public static final int MINUS_ONE = -1;

    public static final String S_DOT = ".".intern();
    
    public static final String S_UNDERSCORE = "_".intern();

    public static final String S_UTF8 = "UTF-8".intern();

    public static final String S_TRUE = "true".intern();

    public static final String S_FALSE = "false".intern();

    public static final String S_GROUP0 = "group0".intern();

    public static final String S_LATEST = "LATEST".intern();
    
    public static final String S_ODISEE_DEBUG = "ODISEE_DEBUG".intern();
    
    public static final String S_ODISEE_PROFILE = "ODISEE_PROFILE".intern();
    
    public static final String S_ODISEE_HOME = "ODISEE_HOME".intern();

    public static final String S_ODISEE_DEPLOY = "ODISEE_DEPLOY".intern();

    public static final String S_ODISEE_VAR = "ODISEE_VAR".intern();

    public static final String S_ODISEE_USER = "ODISEE_USER".intern();

    public static final String S_ODISEE_TMP = "ODISEE_TMP".intern();

    public static final String S_VAR = "var".intern();

    public static final String S_VAR_DEPLOY = "var/deploy".intern();
    
    public static final String S_VAR_TMP = "var/tmp".intern();

    public static final String S_USER = "user".intern();

    public static final String S_TEMPLATE = "template".intern();

    public static final String S_DOCUMENT = "document".intern();

    public static final String S_PDF = "pdf".intern();

    public static final String S_ID = "id".intern();

    public static final String S_REVISION = "revision".intern();

    public static final String S_NOSTREAM = "nostream".intern();

    public static final String FILE_DATEFORMAT_SSSS = "yyyyMMdd-HHmmss_SSSS".intern();

    public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream".intern();

    public static final String MIME_TYPE_ODT = "application/vnd.oasis.opendocument.text".intern();

    public static final String MIME_TYPE_PDF = "application/pdf".intern();

}
