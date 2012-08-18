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
 * rbe, 02.08.12 17:58
 */

package eu.artofcoding.odisee.server;

public class OdiseeServerRuntimeException extends RuntimeException {

    public OdiseeServerRuntimeException() {
    }

    public OdiseeServerRuntimeException(String message) {
        super(message);
    }

    public OdiseeServerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OdiseeServerRuntimeException(Throwable cause) {
        super(cause);
    }

}
