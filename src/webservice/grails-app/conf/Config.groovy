/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

// Odisee home
String ODISEE_HOME = System.getenv('ODISEE_HOME')
if (ODISEE_HOME) {
    /*
    // Odisee configuration
    boolean configFileExists
    File configFile = null
    [
            "${userHome}/.${appName}-webservice-config.properties",
            "${userHome}/.${appName}-webservice-config.groovy",
            "${ODISEE_HOME}/etc/webservice.properties",
            "${ODISEE_HOME}/etc/webservice.groovy",
            System.properties["${appName}.config.location"]
    ].each { cfg ->
        if (cfg) {
            configFile = new File(cfg)
            configFileExists = configFile.exists()
            println "ODI-xxxx: Checking configuration file '${configFile}': it ${configFileExists ? 'exists' : 'does not exist, skipping'}"
            if (configFileExists) {
                println "ODI-xxxx: Adding configuration file '${configFile}'"
                grails.config.locations << configFile
            }
        }
    }
    */
    // Version
    odisee.version = "Version unknown"
    File versionFile = new File(ODISEE_HOME, 'etc/version')
    if (versionFile.exists()) {
        odisee.version = versionFile.text?.trim()
    }
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
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d{dd.MM. HH:mm:ss,SSS} %t[%X{user}] %p %c %m%n')
        //file name: 'file', file: 'odisee.log', layout: pattern(conversionPattern: '%d{dd MMM HH:mm:ss,SSS} %p %t %c{4} %m%n')
    }
    //info stdout: 'grails.app'
    //warn stdout: 'grails.app'
    //error stdout: 'grails.app'
    //debug stdout: 'grails.app'
    //trace stdout: 'grails.app'
    debug console: 'org.codehaus.groovy.grails.web.servlet',
            'grails.app.controller',
            'grails.app.service'
    trace console: 'grails.app.controller',
            'grails.app.service'
}
log4j.additivity.default = false
