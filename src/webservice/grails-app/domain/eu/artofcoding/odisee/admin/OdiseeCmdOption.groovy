/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.admin

/**
 *
 */
enum OdiseeCmdOption {

    HEADLESS("-headless"),
    NO_LOGO("-nologo"),
    NO_FIRST_START_WIZARD("-nofirststartwizard"),
    NO_DEFAULT("-nodefault"),
    NO_CRASH_REPORT("-nocrashreport"),
    NO_RESTART("-norestart"),
    NO_RESTORE("-norestore"),
    NO_LOCK_CHECK("-nolockcheck")

    /**
     * Option string.
     */
    private String cmd

    /**
     * Constructor.
     */
    OdiseeCmdOption(String cmd) {
        this.cmd = cmd
    }

    /**
     * List of possible values (used in GORM constraints).
     */
    static list() {
        [HEADLESS, NO_LOGO, NO_FIRST_START_WIZARD, NO_DEFAULT, NO_CRASH_REPORT, NO_RESTART, NO_RESTORE, NO_LOCK_CHECK]
    }

}

