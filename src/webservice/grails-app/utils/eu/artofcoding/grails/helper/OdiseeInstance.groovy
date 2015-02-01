/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 14.09.14 17:01
 */

package eu.artofcoding.grails.helper

import groovy.util.logging.Log

import java.nio.file.Files
import java.nio.file.Path

import static eu.artofcoding.odisee.OdiseePath.ODISEE_HOME
import static eu.artofcoding.odisee.server.OdiseeConstant.*

@Singleton
@Log
final class OdiseeInstance {

    private final Map oooGroup = [:]

    List<String> readOdiinst() {
        final Path odiinstPath = ODISEE_HOME.resolve(S_ETC_ODIINST)
        final List<String> odiinst = []
        boolean odiinstExists = Files.exists(odiinstPath)
        if (odiinstExists) {
            odiinstPath.eachLine S_UTF8, {
                odiinst << it.split('[|]')
            }
            final Map ipPortGroup = [:]
            final Map<String, List<String>> groupIpPort = odiinst.groupBy { it[1] }
            groupIpPort.each { k, v ->
                ipPortGroup[k] = v.collect { it[2] }
            }
            ipPortGroup.eachWithIndex { it, i ->
                final Map m = [:]
                m[it.key] = it.value
                synchronized (oooGroup) {
                    oooGroup[S_GROUP0] = m
                }
            }
        } else {
            log.warning('ODI-xxxx: No odiinst found, using default 127.0.0.1:2001')
            odiinst << ['odi1', '127.0.0.1', '2001', '', '', '', 'false']
            oooGroup[S_GROUP0] = ['127.0.0.1': 2001]
        }
        odiinst
    }

}
