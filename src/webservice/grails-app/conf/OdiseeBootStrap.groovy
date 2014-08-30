/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

import eu.artofcoding.odisee.OdiseePath

class OdiseeBootStrap {

    def grailsApplication

    def init = { servletContext ->
        println """
           _ \\      |_)               
          |   |  _` | |  __|  _ \\  _ \\
          |   | (   | |\\__ \\  __/  __/
         \\___/ \\__,_|_|____/\\___|\\___| ${grailsApplication.config.odisee.version}
         
         Server initialized.
         
"""
        OdiseePath.dumpEnv()
    }

    def destroy = {
        println """
           ____      ___              
          / __ \\____/ (_)_______  ___ 
         / / / / __  / / ___/ _ \\/ _ \\
        / /_/ / /_/ / (__  )  __/  __/
        \\____/\\__,_/_/____/\\___/\\___/ ${grailsApplication.config.odisee.version}
         
         Server stopped.
         
"""
    }

}
