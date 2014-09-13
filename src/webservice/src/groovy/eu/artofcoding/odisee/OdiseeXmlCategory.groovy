/*
 * odisee
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 15.11.12 12:08
 */
package eu.artofcoding.odisee

import com.sun.star.lang.XComponent
import eu.artofcoding.odisee.helper.Profile
import eu.artofcoding.odisee.ooo.*
import eu.artofcoding.odisee.server.OfficeConnection
import eu.artofcoding.odisee.server.OfficeConnectionFactory

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static eu.artofcoding.odisee.server.OdiseeConstant.*

/**
 * Apply values and instructions from a simple XML file to generate an OpenOffice document.
 * A document is an instance of a certain revision of a template.
 * $ODISEE_VAR/template/name without extension/revision/name_revision.ott
 * $ODISEE_VAR/document/name without extension/name_revision.odt, .pdf
 */
class OdiseeXmlCategory {

    /**
     * Find latest revision of a file with name following this convention:
     * name_revN.ext
     */
    static Path findLatestRevision(Path dir) {
        if (!Files.exists(dir)) {
            throw new OdiseeException("Cannot find template in directory '${dir}'")
        }
        Path[] fs = dir.listFiles()
        // TODO NullPointer when cN == directory
        fs.inject fs[0], { Path o, Path n ->
            // Strip extension and find _rev in filename
            def c1 = (o.name - ~WRITER_EXT_REGEX).split(S_UNDERSCORE).find { it ==~ REVISION_REGEX }
            def c2 = (n.name - ~WRITER_EXT_REGEX).split(S_UNDERSCORE).find { it ==~ REVISION_REGEX }
            c2.compareTo(c1) == 1 ? n : o
        }
    }

    /**
     * Find OpenOffice template by revision and return File object.
     * @param xmlTemplate Node xml.request.template from request XML.
     */
    static Path findTemplate(xmlTemplate) {
        // Check argument
        if (!xmlTemplate) {
            throw new OdiseeException('No template specified')
        }
        // Get data
        String templatePath = xmlTemplate.'@path'?.toString()
        if (!templatePath) {
            throw new OdiseeException('No path to template given')
        }
        String revision = xmlTemplate.'@revision'?.toString()?.toUpperCase()
        // Get template
        Path template = null
        if (templatePath && revision != S_LATEST) {
            template = Paths.get(templatePath)
        }
        // Find latest revision of template
        if (!revision || revision == S_LATEST) {
            Path p = template.parent.resolve(template.fileName)
            template = OdiseeXmlCategory.findLatestRevision(p)
        }
        // Does template exist?
        if (!Files.exists(template)) {
            throw new OdiseeException("ODI-xxxx: Template '${template}' does not exist!")
        }
        template
    }

    /**
     * Process something: execute a closure and call a macro.
     */
    static void processInstruction(XComponent template, closure, macro = null) {
        // Execute pre-macro
        if (macro?.pre?.name) {
            use(OOoDocumentCategory) {
                template.executeMacro(macro.pre.name, (macro.pre.params ?: []) as Object[])
            }
        }
        // Execute closure
        closure(template)
        // Execute post-macro
        if (macro?.post?.name) {
            use(OOoDocumentCategory) {
                template.executeMacro(macro.post.name, (macro.post.params ?: []) as Object[])
            }
        }
    }

    /**
     * Set values in userfields.
     */
    static void processUserfield(XComponent template, userfield) {
        String ufName = userfield.'@name'.toString()
        Profile.time "OOoTextTableCategory.processUserfield(${ufName})", {
            OdiseeXmlCategory.processInstruction template, { t ->
                String ufContent = userfield.text()?.toString() ?: ''
                switch (ufName) {
                // A text table; coordinates
                    case { it ==~ /.*\$.*/ || it ==~ /.*\!.*/ }:
                        use(OOoTextTableCategory) {
                            t[ufName] = ufContent
                        }
                        break
                // Everything else is a variable
                    default:
                        use(OOoFieldCategory) {
                            t[ufName] = ufContent
                        }
                }
            }, [post: [name: userfield.'@post-macro'.toString()]]
        }
    }

    /**
     * Set values in texttables.
     */
    static void processTexttable(XComponent template, texttable) {
        String ttName = texttable.'@name'.toString()
        Profile.time "OOoTextTableCategory.processTexttable(${ttName})", {
            OdiseeXmlCategory.processInstruction template, { t ->
                String ttContent = texttable.text()?.toString() ?: ''
                use(OOoTextTableCategory) {
                    t[ttName] = ttContent
                }
            }, [post: [name: texttable.'@post-macro'.toString()]]
        }
    }

    /**
     * Set text at bookmark.
     */
    static void processBookmark(XComponent template, bookmark) {
        OdiseeXmlCategory.processInstruction template, { t ->
            String bmName = bookmark.'@name'.toString()
            String bmContent = bookmark.text()?.toString() ?: ''
            use(OOoBookmarkCategory) {
                t[bmName] = bmContent
            }
        }, [post: [name: bookmark.'@post-macro'.toString()]]
    }

    /**
     * Insert autotext.
     */
    static void processAutotext(XComponent template, autotext) {
        //
        String autotextGroup = autotext.'@group'.toString() ?: 'Standard'
        String autotextName = autotext.'@name'.toString()
        String bookmark = autotext.'@bookmark'.toString()
        String atend = autotext.'@atend'.toString() == S_TRUE
        //
        OdiseeXmlCategory.processInstruction template, { t ->
            use(OOoAutotextCategory) {
                if (bookmark) {
                    t.insertAutotextAtBookmark(autotextGroup, autotextName, bookmark)
                } else if (atend) {
                    t.insertAutotextAtEnd(autotextGroup, autotextName)
                }
            }
        }, [post: [name: autotext.'@post-macro'.toString()]]
    }

    /**
     * Insert an image.
     */
    static void processImage(XComponent template, image) {
    }

    /**
     * Execute a macro.
     */
    static void processMacro(XComponent template, macro) {
        // Get name, location and language of macro
        String macroName = macro.'@name'.toString()
        String location = macro.'@location'.toString() ?: 'document'
        String language = macro.'@language'.toString() ?: 'Basic'
        String macroUrl = "${macroName}?language=${language}&location=${location}"
        //
        OdiseeXmlCategory.processInstruction template, { t ->
            int paramCount = macro.parameter.size()
            Object[] params = null
            if (paramCount > 0) {
                params = new Object[paramCount]
                macro.parameter.eachWithIndex { p, i ->
                    params[i] = p.toString()
                }
            } else {
                params = [] as Object[]
            }
            use(OOoDocumentCategory) {
                template.executeMacro(macroUrl, params)
            }
        }
    }

    /**
     * Read request and return map.
     */
    static Map readRequest(Path file, int requestNumber) {
        Map arg = [:]
        /*
        // TODO Use validating XmlSlurper
        validator = javax.xml.validation.SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new javax.xml.transform.stream.StreamSource(xsdStream))
                .newValidator()
        // XmlSlurper for reading XML
        xmlSlurper = new XmlSlurper()
        // Read XML using locally cached DTDs
        xmlSlurper.setEntityResolver(com.bensmann.griffon.CachedDTD.entityResolver)
        */
        arg.xml = new XmlSlurper().parseText(file.getText(S_UTF8))
        //
        def request = arg.xml.request[requestNumber]
        // The ID, if none given use actual date and time
        arg.id = request.'@id'?.toString()
        arg.id ?: (arg.id = new Date().format(FILE_DATEFORMAT_SSSS))
        // Get File reference to certain or latest revision of template
        arg.template = OdiseeXmlCategory.findTemplate(request.template)
        // Set revision from found template
        arg.revision = (arg.template.fileName.toString() - ~WRITER_EXT_REGEX).split('_rev').last()
        // Return map
        arg
    }

    /**
     *
     * @param request
     * @param arg
     * @param oooConnection
     * @return
     */
    static List<Path> processTemplate(Map arg, OfficeConnection oooConnection) {
        // Result is one or more document(s)
        List<Path> output = []
        // Get XML request element
        def request = arg.xml.request[arg.activeRequestIndex]
        def template = request.template[0]
        // Set basename for document(s) to generate: dir for template, name of template including revision and ID
        // TODO name must be generated to avoid name clashes with multiple requests
        String documentBasename = null
        if (request.'@name') {
            documentBasename = request.'@name'.toString()
            Charset utf8 = Charset.forName('UTF-8')
            byte[] requestNameAsUTF8 = documentBasename.getBytes(utf8)
            documentBasename = new String(requestNameAsUTF8, utf8)
        } else {
            documentBasename = "${arg.template.fileName.toString().split('\\.')[0..-2].join('.')}-id${arg.id}"
        }
        use(OOoDocumentCategory) {
            // Should we hide OpenOffice?
            boolean hidden = Boolean.TRUE
            // User supplied debug attribute
            boolean localDebug = Boolean.valueOf(request.'@local-debug'.toString())
            if (localDebug) {
                // local-debug="true" ... so OpenOffice's window should shown (Hidden attribute is false)
                hidden = !localDebug
            }
            // Create new document from template
            XComponent xComponent = arg.template.open(oooConnection, [Hidden: hidden])
            // Process all instructions
            String methodName = null
            Profile.time "OdiseeXmlCategory.toDocument(${request.'@name'})", {
                request.instructions.'*'.each { instr ->
                    String tagName = instr.name()
                    String instruction = instr.'@name'
                    Profile.time "OdiseeXmlCategory.toDocument(${request.'@name'}, instruction ${instruction})", {
                        try {
                            // Construct method name from element name
                            methodName = tagName[0].toUpperCase() + tagName[1..-1]
                            OdiseeXmlCategory."process${methodName}"(xComponent, instr)
                        } catch (e) {
                            println "ODI-xxxx: Could not execute instruction '${tagName} ${instruction}': ${e}"
                        }
                    }
                }
            }
            // TODO Refresh document before executing macro? template.refresh()
            // Execute pre-save macro
            String preSaveMacro = template.'@pre-save-macro'.toString()
            if (preSaveMacro) {
                xComponent.executeMacro(preSaveMacro)
            }
            // Create File references for outputFormats from XML
            String outputPath = template.'@outputPath'.toString()
            Path outputDir = Paths.get(outputPath)
            template.'@outputFormat'?.toString()?.split(',')?.each { format ->
                output << outputDir.resolve("${documentBasename}.${format}")
            }
            // Save document to disk
            output.each { Path file ->
                // Ensure existance of directory for document
                Files.createDirectories(file.parent)
                xComponent.saveAs(file)
            }
            // Execute post-save macro
            String postSaveMacro = template.'@post-save-macro'.toString()
            if (postSaveMacro) {
                xComponent.executeMacro(postSaveMacro)
            }
            // Close document
            xComponent.close()
        }
        // Return generated document(s)
        output
    }

    /**
     * Read a XML file and generate an OpenOffice document.
     * @param file The Odisee XML request file to operate on.
     * @param oooConnectionManager
     * @param requestNumber If multiple requests are contained in the XML file, the number of the request to process, defaults to 0.
     * @param requestOverride
     * @return Map
     */
    static Map toDocument(Path file, OfficeConnectionFactory officeConnectionFactory, int requestNumber, requestOverride = null) {
        long start = System.nanoTime()
        // Read XML from file
        Map arg = readRequest(file, requestNumber)
        // The request
        arg.activeRequestIndex = requestNumber
        def request = arg.xml.request[requestNumber]
        // Add overrides
        if (requestOverride) {
            arg += requestOverride
        }
        // The connection
        OfficeConnection oooConnection = null
        List<File> output = null
        // Our return value is a map with timing and output information
        Map result = [output: [], retries: 0, wallTime: -1]
        try {
            // Get connection to OpenOffice
            String group = 'group0' // TODO request.ooo.'@group'.toString()
            oooConnection = officeConnectionFactory.fetchConnection(false)
            if (!oooConnection) {
                throw new OdiseeException("Could not acquire connection from group '${group}'")
            }
            // Process template
            output = OdiseeXmlCategory.processTemplate(arg, oooConnection)
            if (output) {
                result.output += output
            }
            // Wall clock time
            result.wallTime += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
            // Check result
            if (output?.size() == 0) {
                // TODO Should this class decide this?
                if (oooConnection) {
                    oooConnection.setFaulted(true)
                }
                throw new OdiseeException('No document')
            }
        } catch (e) {
            throw e
        } finally {
            // Release connection to pool
            if (oooConnection) {
                officeConnectionFactory.repositConnection(oooConnection)
            }
        }
        result
    }

}
