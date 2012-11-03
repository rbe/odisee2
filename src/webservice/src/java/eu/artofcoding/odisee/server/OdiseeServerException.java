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
 * rbe, 02.08.12 17:27
 */

package eu.artofcoding.odisee.server;

public class OdiseeServerException extends Exception {

    public OdiseeServerException() {
    }

    public OdiseeServerException(String message) {
        super(message);
    }

    public OdiseeServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public OdiseeServerException(Throwable cause) {
        super(cause);
    }

/*
    public OdiseeServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
*/

}
