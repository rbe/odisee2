/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.image

import eu.artofcoding.flux.grails.helper.FileHelper
import eu.artofcoding.flux.grails.helper.StringHelper

/**
 * Category to use ImageMagick with java.io.File. Provides a fluent API.
 * @author rbe
 */
final class ImageMagickCategory {

    /**
     * Initialize.
     * @param self java.io.File
     * @param imageMagickHome HOME of ImageMagick.
     * @return java.io.File
     */
    public static File init(File self, String imageMagickHome) {
        self.metaClass._imStorage = [imageMagickHome: imageMagickHome, option: []]
        self.metaClass.getImStorage = {-> _imStorage }
        self
    }

    /**
     * Set options for ImageMagick.
     * @param self java.io.File
     * @param option String The option, include dash.
     * @param value String The value (optional).
     * @return File
     */
    public static File option(File self, String option, String value = null) {
        // Put option/value into map
        self.imStorage.'option' << option << value
        self
    }

    /**
     * @param self java.io.File
     * @param opt
     * @return Process The Process object used to call ImageMagick.
     * @see java.lang.Process
     */
    public static Process startImageMagick(File self, String... opt) {
        // Setup ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(opt)
        // Set working directory
        processBuilder.directory(self.parentFile)
        // Start process
        Process process = null
        try {
            process = processBuilder.start()
            process.waitFor()
        } catch (e) {
            e.printStackTrace()
        } finally {
            // Return Process object for ImageMagick
            return process
        }
    }

    /**
     * Convert an image through ImageMagick's convert command.
     * @param self java.io.File
     * @return Process The Process object used to call ImageMagick.
     * @see java.lang.Process
     */
    public static Process convert(File self, File imageDest) {
        // Make string array with command and options for ProcessBuilder
        def opt = ["${self.imStorage.'imageMagickHome'}/bin/convert"] <<
                "'" + self.absolutePath + "'" <<
                self.imStorage.'option'.findAll { null != it } <<
                "'" + imageDest.absolutePath + "'"
        // Build shell script
        File script = File.createTempFile('im_', '.sh')
        script.withWriter { w -> w.write(opt.flatten().join(' ')) }
        // Start ImageMagick process
        opt = ['/bin/bash', script.absolutePath]
        Process process = null
        try {
            process = self.startImageMagick(opt.flatten() as String[])
        } catch (e) {
            e.printStackTrace()
            throw e
        } finally {
            // Delete script file
            script.deleteOnExit()
            script.delete()
            // Return Process object
            return process
        }
    }

    /**
     * Identify: get information from an image.
     * @param self java.io.File
     * @return Process The Process object used to call ImageMagick.
     * @see java.util.Map
     */
    public static Map identify(File self) {
        // Make string array with command and options for ProcessBuilder
        def opt = ["${self.imStorage.'imageMagickHome'}/bin/identify"] <<
                self.imStorage.'option'.findAll { null != it } <<
                "'" + self.absolutePath + "'"
        Process process = null
        Map info = [:]
        try {
            // Start ImageMagick process
            process = self.startImageMagick(opt.flatten() as String[])
            String[] split = null
            String key = null
            String value = null
            //
            List<String> prefix = new Vector<String>(4); 0.upto 3, { prefix.add(null) }
            // Parse lines
            process.inputStream.eachLine { line ->
                split = line.split(': ')
                println split
                // Just a key indicates a new 'section'
                if (split.length == 1) {
                    key = split[0].replaceAll(':', '')
                } else { // Key: value
                    key = split[0..-2].join('_').replaceAll(':', '-')
                }
                // Keys should be w/o leading or trailing spaces, lower case, and w/o spaces, comma
                key = key.trim().toLowerCase().replaceAll('[ ,]', '-')
                value = split[-1].trim()
                // Count spaces
                int spaceCount = StringHelper.countSpacesAtBeginning(split[0])
                if (spaceCount == 0) {
                    return
                } else if (spaceCount == 2) { // New prefix
                    prefix = prefix.collect { null }
                    prefix.set(0, key)
                } else if (spaceCount > 2) {
                    prefix.set((int) (spaceCount / 2 - 1), key)
                }
                // Key: value
                if (split.length >= 2) {
                    info[prefix.findAll { null != it }.join('-')] = value ?: null
                }
            }
        } catch (e) {
            info = null
            throw e
        } finally {
            return info
        }
    }

    /**
     * Does this file format support transparency?<br/>
     * See http://en.wikipedia.org/wiki/Transparency_(graphic).
     * @see FileHelper#decomposeFilename(java.io.File)
     * @param self {@link java.io.File}.
     * @return boolean File supports/does not support transparency.
     */
    public static boolean supportsTransparency(File self) {
        Map<String, String> decomp = FileHelper.decomposeFilename(self)
        decomp.ext.toLowerCase() in ['gif', 'png', 'tiff', 'tif', 'svg', 'pdf']
    }

}
