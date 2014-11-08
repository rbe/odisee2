/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

import org.apache.log4j.PatternLayout

import java.nio.file.Path
import java.nio.file.Paths

// Odisee home
String ODISEE_HOME = System.getenv('ODISEE_HOME')
if (ODISEE_HOME) {
    /*
    // Odisee configuration
    [
            "${userHome}/.${appName}-webservice-config.properties",
            "${userHome}/.${appName}-webservice-config.groovy",
            "${ODISEE_HOME}/etc/webservice.properties",
            "${ODISEE_HOME}/etc/webservice.groovy",
            System.properties["${appName}.config.location"]
    ].each { cfg ->
        if (cfg) {
            Path configFile = Paths.get(cfg)
            boolean configFileExists = Files.exists(configFile)
            println "ODI-xxxx: Checking configuration file '${configFile}': it ${configFileExists ? 'exists' : 'does not exist, skipping'}"
            if (configFileExists) {
                println "ODI-xxxx: Adding configuration file '${configFile}'"
                grails.config.locations << configFile
            }
        }
    }
    */
    // Version
    Path versionFile = Paths.get(ODISEE_HOME, 'etc/version')
    odisee.version = versionFile.text?.trim()
} else {
    println "ODI-xxxx: Please set ODISEE_HOME."
}

grails.project.groupId = "eu.artofcoding.${appName}" // change this to alter the default package name and Maven publishing destination

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        html: [
                'text/html',
                'application/xhtml+xml'
        ],
        xml: [
                'text/xml',
                'application/xml'
        ],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: [
                'application/json',
                'text/json'
        ],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = 'none' // none, html, base64
grails.views.gsp.encoding = 'UTF-8'
grails.views.javascript.library = 'prototype'
grails.converters.encoding = 'UTF-8'
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true

// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// Our own stacktrace filterer
// TODO grails.logging.stackTraceFiltererClass = 'eu.artofcoding.odisee.helper.StacktraceFilterer'
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// GORM
grails.gorm.failOnError = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://localhost:8080/${appName}"
        log4j = {
            def logLayoutPattern = new PatternLayout('%d{HH:mm:ss,SSS} %p %t %c{4} %m%n')
            appenders {
                file name: 'root_log',          file: "${ODISEE_HOME}/var/log/odisee_root.log",           layout: logLayoutPattern
                file name: 'dev_log',           file: "${ODISEE_HOME}/var/log/odisee_dev.log",            layout: logLayoutPattern
                file name: 'grailsCommons_log', file: "${ODISEE_HOME}/var/log/odisee_grails_commons.log", layout: logLayoutPattern
                file name: 'grailsWeb_log',     file: "${ODISEE_HOME}/var/log/odisee_grails_web.log",     layout: logLayoutPattern
            }
            root {
                error 'stdout', 'root_log'
            }
            error additivity: false, grailsCommons_log: [
                    'org.codehaus.groovy.grails.commons',
            ]
            error additivity: false, grailsWeb_log: [
                    'org.codehaus.groovy.grails.web'
            ]
            error additivity: false, dev_log: [
                    'eu.artofcoding.odisee',
                    'grails.app.controllers',
                    'grails.app.domain',
                    'grails.app.services',
                    'grails.app.taglib',
                    'grails.app.conf',
                    'grails.app.filters'
            ]
        }
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
        log4j = {
            def logLayoutPattern = new PatternLayout('%d{HH:mm:ss,SSS} %p %t %c{4} %m%n')
            appenders {
                file name: 'root_log',          file: "${ODISEE_HOME}/var/log/odisee_root.log",           layout: logLayoutPattern
                file name: 'dev_log',           file: "${ODISEE_HOME}/var/log/odisee_dev.log",            layout: logLayoutPattern
                file name: 'grailsCommons_log', file: "${ODISEE_HOME}/var/log/odisee_grails_commons.log", layout: logLayoutPattern
                file name: 'grailsWeb_log',     file: "${ODISEE_HOME}/var/log/odisee_grails_web.log",     layout: logLayoutPattern
            }
            root {
                error 'stdout', 'root_log'
            }
            all additivity: false, grailsCommons_log: [
                    'org.codehaus.groovy.grails.commons',
            ]
            all additivity: false, grailsWeb_log: [
                    'org.codehaus.groovy.grails.web'
            ]
            all additivity: false, dev_log: [
                    'eu.artofcoding.odisee',
                    'grails.app.controllers',
                    'grails.app.domain',
                    'grails.app.services',
                    'grails.app.taglib',
                    'grails.app.conf',
                    'grails.app.filters'
            ]
        }
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
        log4j = {
            def logLayoutPattern = new PatternLayout('%d{HH:mm:ss,SSS} %p %t %c{4} %m%n')
            appenders {
                file name: 'root_log',          file: "${ODISEE_HOME}/var/log/odisee_root.log",           layout: logLayoutPattern
                file name: 'dev_log',           file: "${ODISEE_HOME}/var/log/odisee_dev.log",            layout: logLayoutPattern
                file name: 'grailsCommons_log', file: "${ODISEE_HOME}/var/log/odisee_grails_commons.log", layout: logLayoutPattern
                file name: 'grailsWeb_log',     file: "${ODISEE_HOME}/var/log/odisee_grails_web.log",     layout: logLayoutPattern
            }
            root {
                error 'stdout', 'root_log'
            }
            all additivity: false, grailsCommons_log: [
                    'org.codehaus.groovy.grails.commons',
            ]
            all additivity: false, grailsWeb_log: [
                    'org.codehaus.groovy.grails.web'
            ]
            all additivity: false, dev_log: [
                    'eu.artofcoding.odisee',
                    'grails.app.controllers',
                    'grails.app.domain',
                    'grails.app.services',
                    'grails.app.taglib',
                    'grails.app.conf',
                    'grails.app.filters'
            ]
        }
    }
}
