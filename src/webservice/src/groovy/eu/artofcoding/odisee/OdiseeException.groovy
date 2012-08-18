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

    OdiseeException(Throwable throwable) {
        super(throwable)
    }

    OdiseeException(String message) {
        super(message)
    }

    OdiseeException(String message, Throwable throwable) {
        super(message, throwable)
    }

}
