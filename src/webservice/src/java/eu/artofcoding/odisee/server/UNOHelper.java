/*
 * odisee
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 30.11.12 09:20
 */

package eu.artofcoding.odisee.server;

import com.sun.star.lib.uno.helper.UnoUrl;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class UNOHelper {

    public static String makeUnoUrl(InetSocketAddress socketAddress) {
        return "socket,host=" + socketAddress.getHostName() + ",port=" + socketAddress.getPort() + ";urp;StarOffice.ServiceManager";
    }

    public static int getHost(String unoURL) throws OdiseeServerException {
        HashMap connectionParameters = null;
        try {
            connectionParameters = UnoUrl.parseUnoUrl(unoURL).getConnectionParameters();
            return Integer.valueOf((String) connectionParameters.get("host"));
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException("Cannot extract host from UNOHelper URL", e);
        }
    }

    public static int getPort(String unoURL) throws OdiseeServerException {
        HashMap connectionParameters = null;
        try {
            connectionParameters = UnoUrl.parseUnoUrl(unoURL).getConnectionParameters();
            return Integer.valueOf((String) connectionParameters.get("port"));
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException("Cannot extract port from UNOHelper URL", e);
        }
    }

    public static void main(String[] args) throws OdiseeServerException {
        System.out.println(UNOHelper.getPort("uno:socket,host=78.47.242.148,port=2083;urp;StarOffice.ServiceManager"));
    }

}
