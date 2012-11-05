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
 * rbe, 03.08.12 14:31
 */

package eu.artofcoding.odisee.server;

public class OdiseeConstant {

    public static final String NATIVE_TEMPLATE_REGEX = ".*\\.ot[gpst]".intern();

    public static final String NATIVE_DOCUMENT_REGEX = ".*\\.od[gpst]".intern();

    public static final String WRITER_EXT_REGEX = ".o\\wt$".intern();

    public static final String REVISION_REGEX = "rev\\d+".intern();

    private static final int THOUSAND = 1000;

    public static final int TIMEOUT_1SEC = 1 * THOUSAND;

    public static final int UNUSABLE_TIMEOUT = 30 * THOUSAND;

    public static final int SPIN_TIMEOUT = 1 * THOUSAND;

    public static final String S_UTF8 = "UTF-8".intern();

    public static final String S_TRUE = "true".intern();

    public static final String S_FALSE = "false".intern();

    public static final String S_GROUP0 = "group0".intern();

    public static final String S_UNDERSCORE = "_".intern();

    public static final String S_LATEST = "LATEST".intern();

    public static final String S_ODISEE_HOME = "ODISEE_HOME".intern();

    public static final String S_ODISEE_DEPLOY = "ODISEE_DEPLOY".intern();

    public static final String S_ODISEE_VAR = "ODISEE_VAR".intern();

    public static final String S_ODISEE_USER = "ODISEE_USER".intern();

    public static final String S_ODISEE_TMP = "ODISEE_TMP".intern();

    public static final String S_VAR = "var".intern();

    public static final String S_VAR_TMP = "var/tmp".intern();

    public static final String S_USER = "user".intern();

    public static final String S_TEMPLATE = "template".intern();

    public static final String S_DOCUMENT = "document".intern();

    public static final String S_PDF = "pdf".intern();

}
