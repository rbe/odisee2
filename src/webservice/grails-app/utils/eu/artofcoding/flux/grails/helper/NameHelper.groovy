/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.flux.grails.helper

import org.codehaus.groovy.grails.commons.GrailsClass

/**
 * Helper for names.
 */
class NameHelper {

    /**
     * Transactional?
     */
    boolean transactional = false

    /**
     * Uppercase first letter and add the rest.
     */
    def ucFirst(s) {
        if (s?.length() > 2) {
            s[0].toUpperCase() + s.substring(1)
        } else
            s
    }

    /**
     * Lowercase first letter and add the rest.
     */
    def lcFirst(s) {
        if (s?.length() > 2) {
            s[0].toLowerCase() + s.substring(1)
        } else
            s
    }

    /**
     * HelloWorld -> Hello_World
     * helloWorld -> hello_World
     */
    def mapCamelCaseToUnderscore(s) {
        def buf = new StringBuilder()
        s.eachWithIndex { c, i ->
            if (i > 0 && Character.isUpperCase((char) c)) {
                buf << "_"
            }
            buf << c
        }
        buf.charAt(0) == "_" ? buf.substring(1) : buf.toString()
    }

    /**
     * Hello_World -> HelloWorld
     * hello_World -> helloWorld
     */
    static def mapUnderscoreToCamelCase(s) {
        def buf = new StringBuilder()
        s.eachWithIndex { c, i ->
            if (c == "_") {
            } else if (i > 0 && s.charAt(i - 1) == "_") {
                buf << c.toUpperCase()
            } else {
                buf << c
            }
        }
        buf.toString()
    }

    /**
     * Hello-World -> HelloWorld
     * hello-World -> helloWorld
     */
    static def mapDashToCamelCase(s) {
        def buf = new StringBuilder()
        s.eachWithIndex { c, i ->
            if (c == "-") {
            } else if (i > 0 && s.charAt(i - 1) == "-") {
                buf << c.toUpperCase()
            } else {
                buf << c
            }
        }
        buf.toString()
    }

    /**
     * Returns the "base" name of a controller or service by stripping off well known
     * Grails suffixes.
     */
    def getBaseName(n) {
        ["Controller", "Service", "TagLib"].each {
            n = n.replaceAll(it, "")
        }
        n
    }

    /**
     * Get corresponding domain class name for a artefact.
     */
    def toDomainPropertyName(c) {
        def name
        if (c instanceof GrailsClass)
            name = c.name
        else if (c instanceof String)
            name = c
        "${getBaseName(name[0].toLowerCase() + name.substring(1))}"
    }

    /**
     * Get corresponding service name for a artefact.
     */
    def toServicePropertyName(c) {
        def name
        if (c instanceof GrailsClass)
            name = c.name
        else if (c instanceof String)
            name = c
        "${getBaseName(name[0].toLowerCase() + name.substring(1))}Service"
    }

    /**
     * Get corresponding domain class name for a artefact.
     */
    def toDomainClassName(c) {
        def name
        if (c instanceof GrailsClass)
            name = c.name
        else if (c instanceof String)
            name = c
        "${getBaseName(name[0].toUpperCase() + name.substring(1))}"
    }

    /**
     * Get corresponding service name for a artefact.
     */
    def toServiceClassName(c) {
        def name
        if (c instanceof GrailsClass)
            name = c.name
        else if (c instanceof String)
            name = c
        "${getBaseName(name[0].toUpperCase() + name.substring(1))}Service"
    }

    /**
     * Get fully-qualified corresponding domain class name for a artefact.
     */
    def toFullDomainClassName(c) {
        def name
        if (c instanceof GrailsClass)
            name = "${c.packageName}.${c.name}"
        else if (c instanceof String)
            name = c
        getBaseName(name)
    }

    /**
     * Get fully-qualified corresponding service name for a artefact.
     */
    def toFullServiceClassName(c) {
        def name
        if (c instanceof GrailsClass)
            name = "${c.packageName}.${c.name}"
        else if (c instanceof String)
            name = c
        "${getBaseName(name)}Service"
    }

    /**
     * Get the domain class part from <domain>_<property>_<id>.
     */
    def getDomainClassPart(n) {
        n.split("\\_")[0].replaceAll("\\#", ".")
    }

    /**
     * Get the id part from <domain>_<property>_<id>.
     */
    def getIdPart(n) {
        n.split("\\_")[1]
    }

    /**
     * Get the property part from <domain>_<property>_<id>.
     */
    def getPropertyPart(n) {
        n.split("\\_")[2]
    }

    /**
     * Generate a unique name.
     */
    def uniqueName() {
        java.util.UUID.randomUUID().toString()[0..7]
    }

}
