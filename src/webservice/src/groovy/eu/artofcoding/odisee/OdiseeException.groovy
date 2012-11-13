/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee

/**
 * The one and only exception in Odisee.
 */
class OdiseeException extends RuntimeException {

    OdiseeException() {
    }

    OdiseeException(Throwable throwable) {
        super(throwable)
    }

    OdiseeException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1)
    }

    OdiseeException(String message) {
        super(message)
    }

    OdiseeException(String message, Throwable throwable) {
        super(message, throwable)
    }

}
