/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.grails.helper

/**
 *
 * @author rbe
 */
final class FileHelper {

    private static final String S_UTF8 = 'UTF-8'
    private static final String S_NAME = 'name'
    private static final String S_EXT = 'ext'

    /**
     * Decompose a filename in name and extension.
     * @param file
     * @return Map Keys: name, ext.
     */
    static Map decomposeFilename(String filename) {
        Map map = [:]
        if (filename) {
            if (filename.contains('.')) {
                String[] split = filename.split('[.]')
                map[S_NAME] = split[0..-2].join('')
                map[S_EXT] = split[-1] //.toLowerCase()
            } else {
                map[S_NAME] = filename
                map[S_EXT] = 'xxx'
            }
        }
        // Return
        map
    }

    /**
     * Convenience method for {@link decomposeFilename ( String )}.
     * @see #decomposeFilename(String)
     * @param file
     * @return Map Keys: name, ext.
     */
    static Map decomposeFilename(File file) {
        decomposeFilename(file?.name)
    }

    /**
     * Check if two file have the same format/extension or not.
     * @param file1 Left/first file for check.
     * @param file2 Right/second file for check.
     * @return boolean
     */
    static boolean isDifferentFileFormat(File file1, File file2) {
        // Decompose both filenames
        Map<String, String> decomp1 = FileHelper.decomposeFilename(file1)
        Map<String, String> decomp2 = FileHelper.decomposeFilename(file2)
        // Extensions different?
        decomp1.ext.toLowerCase() != decomp2.ext.toLowerCase()
    }

    /**
     * Write some bytes to an UTF-8 encoded file.
     * @param file
     * @param str
     */
    static void writeUTF8(File file, String str) {
        file.withWriter S_UTF8, { writer ->
            writer.write(str)
        }
    }

}
