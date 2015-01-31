/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 30.11.12 09:20
 */

package eu.artofcoding.odisee.server;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.UnoUrl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

public final class UnoHelper {

    private UnoHelper() {
        throw new AssertionError();
    }

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

    public static PropertyValue makePropertyValue(final String name, final Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    public static void addPropertyValue(final List<PropertyValue> properties, final String name, final Object value) {
        PropertyValue propertyValue = makePropertyValue(name, value);
        properties.add(propertyValue);
    }

    public static PropertyValue makePropertyValue(final String name, final int value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    public static void addPropertyValue(final List<PropertyValue> properties, final String name, final int value) {
        PropertyValue propertyValue = makePropertyValue(name, value);
        properties.add(propertyValue);
    }

    public static PropertyValue makePropertyValue(final String name, final boolean value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    public static void addPropertyValue(final List<PropertyValue> properties, final String name, final boolean value) {
        PropertyValue propertyValue = makePropertyValue(name, value);
        properties.add(propertyValue);
    }

    public static void main(String[] args) throws OdiseeServerException {
        System.out.println(UnoHelper.getPort("uno:socket,host=78.47.242.148,port=2083;urp;StarOffice.ServiceManager"));
    }

}
