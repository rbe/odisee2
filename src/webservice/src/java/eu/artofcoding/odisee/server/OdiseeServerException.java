/*
 * odisee
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 30.10.12 09:01
 */

package eu.artofcoding.odisee.server;

public class OdiseeServerException extends Exception {

    public OdiseeServerException(String message) {
        super(message);
    }

    public OdiseeServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public OdiseeServerException(Throwable cause) {
        super(cause);
    }

}
