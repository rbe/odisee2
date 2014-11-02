/*
 * odisee2
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 13.09.14 19:55
 */

package eu.artofcoding.grails.helper

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class Compression {

    public static isCompressed(InputStream input) {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(input, 2);
        byte[] signature = new byte[2];
        pushbackInputStream.read(signature);
        pushbackInputStream.unread(signature);
        return signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b;
    }

    /**
     * Check if InputStream is gzip'ed by looking at the first two bytes (magic number)
     * and if it is, return a GZIPInputStream wrapped stream.
     * @param input An input stream.
     * @return The input or GZIPInputStream(input).
     */
    public static InputStream decompressStream(InputStream input) {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(input, 2);
        byte[] signature = new byte[2];
        pushbackInputStream.read(signature);
        pushbackInputStream.unread(signature);
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) {
            GZIPInputStream stream = new GZIPInputStream(pushbackInputStream);
            return stream;
        } else {
            return pushbackInputStream;
        }
    }

    /**
     * Compress a byte[] using gzip.
     * @param b The byte[] to compress.
     * @params base64 Encode bytes as base64?
     * @return byte[]
     */
    public static byte[] zip(byte[] b, boolean base64 = false) {
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream()
        GZIPOutputStream zipStream = new GZIPOutputStream(targetStream)
        zipStream.write(b)
        zipStream.close()
        if (base64) {
            byte[] zipped = targetStream.toByteArray()
            targetStream.close()
            Writable encodeBase64 = zipped.encodeBase64()
            byte[] bytes = encodeBase64.toString().bytes
            return bytes
        } else {
            byte[] bytes = targetStream.toByteArray()
            return bytes
        }
    }

    /**
     * Convenience method for: {@link #zip(byte [])}
     * @param str The string to compress using gzip.
     * @return byte[]
     */
    public static byte[] zip(String str) {
        return zip(str.bytes)
    }

}
