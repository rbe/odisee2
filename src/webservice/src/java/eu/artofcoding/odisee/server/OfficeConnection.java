/*
 * odisee
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 03.11.12 14:13
 */

package eu.artofcoding.odisee.server;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.BridgeExistsException;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.Connector;
import com.sun.star.connection.NoConnectException;
import com.sun.star.connection.XConnection;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lib.uno.helper.UnoUrl;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.sun.star.uno.UnoRuntime.queryInterface;

public class OfficeConnection {

    /**
     * Was this instance already successfully bootstraped?
     */
    private boolean wasBootstrappedAlready;

    private boolean connected;

    private List<OfficeDocument> managedDocuments;

    /**
     * Host and port.
     */
    private InetSocketAddress socketAddress;

    /**
     * The UNO URL.
     */
    private String unoURL;

    /**
     * The remote XMultiComponentFactory.
     */
    private XMultiComponentFactory xRemoteServiceManager;

    /**
     * The XBridge.
     */
    private XBridge xBridge;

    /**
     * The remote desktop.
     */
    private Object desktop;

    /**
     * The component loader.
     */
    private XComponentLoader xComponentLoader;

    /**
     * Constructor.
     */
    public OfficeConnection(InetSocketAddress socketAddress) {
        managedDocuments = new ArrayList<OfficeDocument>();
        this.socketAddress = socketAddress;
    }

    @Override
    public String toString() {
        return "OfficeConnection{" +
                "unoURL=" + unoURL +
                '}';
    }

    public boolean initializationCompleted() {
        boolean b = false;
        if (wasBootstrappedAlready) {
            b = true;
        }
        return b;
    }

    //<editor-fold desc="Bootstrap, Connections">

    /**
     * Bootstrap local office.
     * @throws OdiseeServerException
     */
    public void bootstrapLocalOffice() throws OdiseeServerException {
        // Check state
        if (initializationCompleted()) {
            throw new OdiseeServerRuntimeException("Cannot initialize twice");
        }
        // Get the remote office component context
        XComponentContext xRemoteContext;
        try {
            xRemoteContext = Bootstrap.bootstrap();
            if (xRemoteContext == null) {
                throw new OdiseeServerException("Could not bootstrap default office, no remote XComponentContext");
            }
            try {
                // Get remote service manager
                xRemoteServiceManager = xRemoteContext.getServiceManager();
                // Create desktop service
                desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);
                // Query the XComponentLoader interface from the desktop
                xComponentLoader = queryInterface(XComponentLoader.class, desktop);
                // This instance was bootstrapped already
                wasBootstrappedAlready = true;
            } catch (com.sun.star.uno.Exception e) {
                throw new OdiseeServerException(e);
            }
        } catch (BootstrapException e) {
            throw new OdiseeServerException("Cannot connect", e);
        }
    }

    /**
     * Bootstrap local office as a TCP/IP server.
     * @param socketAddress TCP/IP address, host and port.
     * @param start         If true we try to start the Office instance.
     * @throws OdiseeServerException
     */
    public void bootstrap(InetSocketAddress socketAddress, boolean start) throws OdiseeServerException {
        /*
        // Check state
        if (null != socketAddress) {
            throw new OdiseeServerException("Already bootstrapped, address is " + socketAddress);
        }
        */
        this.socketAddress = socketAddress;
        unoURL = UnoHelper.makeUnoUrl(socketAddress);
        // Start a process if address is 127.0.0.1
        if (start && socketAddress.getHostName().equals("127.0.0.1")) {
            OfficeProcess officeProcess = new OfficeProcess();
            officeProcess.startOfficeProcess(socketAddress);
        }
    }

    public void bootstrap(boolean start) throws OdiseeServerException {
        if (null != socketAddress) {
            bootstrap(socketAddress, start);
        } else {
            bootstrapLocalOffice();
        }
        enumerateComponents("bootstrap");
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect() throws OdiseeServerException {
        // Check state
        if (isConnected() && initializationCompleted()) {
            //throw new OdiseeServerRuntimeException("Cannot initialize twice");
            return;
        }
        // Create default local component context
        XComponentContext xLocalContext = null;
        try {
            xLocalContext = Bootstrap.createInitialComponentContext(null);
        } catch (Exception e) {
            throw new OdiseeServerException("Cannot create intial component context", e);
        }
        //
        // Initial service manager
        //
        XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
        //
        // XBridgeFactory
        //
        Object oBridgeFactory = null;
        try {
            oBridgeFactory = xLocalServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", xLocalContext);
        } catch (com.sun.star.uno.Exception e) {
            throw new OdiseeServerException("Could not create BridgeFactory", e);
        }
        XBridgeFactory factory = queryInterface(XBridgeFactory.class, oBridgeFactory);
        UnoUrl parsedUnoUrl = null;
        try {
            parsedUnoUrl = UnoUrl.parseUnoUrl(unoURL);
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException("Could not parse UNOHelper url: " + unoURL, e);
        }
        if (null == parsedUnoUrl) {
            throw new OdiseeServerException("Could not parse UNOHelper url: " + unoURL);
        }
        //
        // XConnection
        //
        XConnection xConnection = null;
        try {
            xConnection = Connector.create(xLocalContext).connect(parsedUnoUrl.getConnectionAndParametersAsString());
        } catch (NoConnectException e) {
            throw new OdiseeServerException("Could not connect to " + unoURL, e);
        } catch (ConnectionSetupException e) {
            throw new OdiseeServerException("Could not connect to " + unoURL, e);
        }
        if (null == xConnection) {
            throw new OdiseeServerException("Could not connect to " + unoURL);
        }
        try {
            xBridge = factory.createBridge("", parsedUnoUrl.getProtocolAndParametersAsString(), xConnection, null);
        } catch (BridgeExistsException e) {
            throw new OdiseeServerException("Could not setup XBridge", e);
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException("Could not setup XBridge", e);
        }
        //
        // Get the remote service manager
        //
        Object o = xBridge.getInstance("StarOffice.ServiceManager");
        xRemoteServiceManager = queryInterface(XMultiComponentFactory.class, o);
        // Retrieve the component context (it's not yet exported from the office)
        // Query for the XPropertySet interface.
        XPropertySet xProperySet = (XPropertySet) queryInterface(XPropertySet.class, xRemoteServiceManager);
        // Get the default context from the office server.
        Object oDefaultContext = null;
        try {
            oDefaultContext = xProperySet.getPropertyValue("DefaultContext");
        } catch (UnknownPropertyException e) {
            throw new OdiseeServerException("Cannot get DefaultContext", e);
        } catch (WrappedTargetException e) {
            throw new OdiseeServerException("Cannot get DefaultContext", e);
        }
        // Query for the interface XComponentContext.
        XComponentContext xComponentContext = queryInterface(XComponentContext.class, oDefaultContext);
        // Get remote service manager
        xRemoteServiceManager = xComponentContext.getServiceManager();
        // Now create the desktop service
        // NOTE: use the office component context here!
        try {
            desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xComponentContext);
        } catch (com.sun.star.uno.Exception e) {
            throw new OdiseeServerException("Cannot get Desktop", e);
        }
        // Query the XComponentLoader interface from the desktop
        xComponentLoader = queryInterface(XComponentLoader.class, desktop);
        // This instance was bootstrapped already
        wasBootstrappedAlready = true;
        // Connection is established
        connected = true;
        /*
        // Create a UnoUrlResolver
        Object urlResolver = null;
        try {
            urlResolver = xLocalServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", xLocalContext);
        } catch (com.sun.star.uno.Exception e) {
            throw new OdiseeServerException("Cannot create UNO url resolver", e);
        }
        // Query for the XUnoUrlResolver interface
        XUnoUrlResolver xUrlResolver = queryInterface(XUnoUrlResolver.class, urlResolver);
        Object rInitialObject = null;
        try {
            // Import the object
            rInitialObject = xUrlResolver.resolve(unoURL);
        } catch (com.sun.star.connection.NoConnectException e) {
            throw new OdiseeServerException("Couldn't connect to remote server", e);
        } catch (com.sun.star.connection.ConnectionSetupException e) {
            throw new OdiseeServerException("Couldn't access necessary local resource to establish the interprocess connection", e);
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException("uno-url is syntactical illegal (" + unoURL + ")", e);
        } catch (com.sun.star.uno.RuntimeException e) {
            throw new OdiseeServerException("Got UNO RuntimeException", e);
        }
        if (null != rInitialObject) {
            XMultiComponentFactory xOfficeFactory = queryInterface(XMultiComponentFactory.class, rInitialObject);
            // Retrieve the component context as property (it is not yet exported from the office)
            // Query for the XPropertySet interface.
            XPropertySet xProperySet = queryInterface(XPropertySet.class, xOfficeFactory);
            // Get the default context from the office server.
            Object oDefaultContext = null;
            try {
                oDefaultContext = xProperySet.getPropertyValue("DefaultContext");
            } catch (UnknownPropertyException e) {
                throw new OdiseeServerException("Cannot get DefaultContext", e);
            } catch (WrappedTargetException e) {
                throw new OdiseeServerException("Cannot get DefaultContext", e);
            }
            // Query for the interface XComponentContext.
            XComponentContext xComponentContext = queryInterface(XComponentContext.class, oDefaultContext);
            // Get remote service manager
            xRemoteServiceManager = xComponentContext.getServiceManager();
            // Now create the desktop service
            // NOTE: use the office component context here!
            try {
                desktop = xOfficeFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xComponentContext);
            } catch (com.sun.star.uno.Exception e) {
                throw new OdiseeServerException("Cannot get Desktop", e);
            }
            // Query the XComponentLoader interface from the desktop
            xComponentLoader = queryInterface(XComponentLoader.class, desktop);
            // This instance was bootstrapped already
            wasBootstrappedAlready = true;
        } else {
            throw new OdiseeServerException("Given initial-object name unknown at server side");
        }
        */
    }

    public void setFaulted(boolean faulted) {
        if (faulted) {
            try {
                close();
            } catch (Exception e) {
            }
        }
    }

    public void close() throws OdiseeServerException {
        enumerateComponents("close");
        // Cleanup all managed documents
        for (OfficeDocument officeDocument : managedDocuments) {
            try {
                officeDocument.closeDocument(true);
            } catch (OdiseeServerException e) {
                // ignore
            }
        }
        // Connection is dropped
        connected = false;
        // Dispose the bridge
        if (null != xBridge) {
            try {
                XComponent xBridgeXComponent = queryInterface(XComponent.class, xBridge);
                xBridgeXComponent.dispose();
            } catch (Exception e) {
                // ignore
            }
        }
        // Cleanup references
        desktop = null;
        xRemoteServiceManager = null;
        xComponentLoader = null;
        xBridge = null;
    }

    //</editor-fold>

    //<editor-fold desc="Getters...">

    public XDesktop getXDesktop() {
        // Check state
        if (!initializationCompleted()) {
            throw new OdiseeServerRuntimeException("Not initialized");
        }
        return queryInterface(XDesktop.class, desktop);
    }

    public XMultiComponentFactory getXRemoteServiceManager() {
        // Check state
        if (!initializationCompleted()) {
            throw new OdiseeServerRuntimeException("Not initialized");
        }
        return xRemoteServiceManager;
    }

    public XComponentLoader getXComponentLoader() {
        // Check state
        if (!initializationCompleted()) {
            throw new OdiseeServerRuntimeException("Not initialized");
        }
        return xComponentLoader;
    }

    //</editor-fold>

    //<editor-fold desc="Create documents">

    public OfficeDocument createDocument(OfficeDocumentType type) throws OdiseeServerException {
        OfficeDocument officeDocument = new OfficeDocument(this);
        /*XComponent xComponent = */
        officeDocument.newDocument(type);
        managedDocuments.add(officeDocument);
        return officeDocument;
    }

    public OfficeDocument createDocument(URL url) throws OdiseeServerException {
        OfficeDocument officeDocument = new OfficeDocument(this);
        /*XComponent xComponent = */
        officeDocument.newDocument(url);
        managedDocuments.add(officeDocument);
        return officeDocument;
    }

    public OfficeDocument createDocument(File file) throws OdiseeServerException {
        try {
            return createDocument(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new OdiseeServerException("URL invalid", e);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Save documents">

    public void saveDocument(OfficeDocument officeDocument, URL saveAsUrl) throws OdiseeServerException {
        officeDocument.saveAs(saveAsUrl);
    }

    public void saveAndCloseDocument(OfficeDocument officeDocument, URL saveAsUrl, boolean force) throws OdiseeServerException {
        officeDocument.saveAs(saveAsUrl);
        closeDocument(officeDocument, force);
    }

    //</editor-fold>

    //<editor-fold desc="Close documents">

    public void closeDocument(OfficeDocument officeDocument, boolean force) throws OdiseeServerException {
        officeDocument.closeDocument(force);
        managedDocuments.remove(officeDocument);
    }

    public void closeAllComponents() throws OdiseeServerException {
        XEnumerationAccess xEnumerationAccess = getXDesktop().getComponents();
        if (xEnumerationAccess.hasElements()) {
            XComponent xComponent = null;
            XEnumeration xEnumeration = xEnumerationAccess.createEnumeration();
            while (xEnumeration.hasMoreElements()) {
                try {
                    Any any = (Any) xEnumeration.nextElement();
                    xComponent = (XComponent) any.getObject();
                    queryInterface(XCloseable.class, xComponent).close(true);
                } catch (NoSuchElementException e) {
                    // ignore
                } catch (WrappedTargetException e) {
                    // ignore
                } catch (CloseVetoException e) {
                    throw new OdiseeServerException("Component " + xComponent + "does not agree to close", e);
                }
            }
        }
    }

    //</editor-fold>

    private void enumerateComponents(String action) {
        try {
            XEnumerationAccess xEnumerationAccess = getXDesktop().getComponents();
            if (xEnumerationAccess.hasElements()) {
                XEnumeration xEnumeration = xEnumerationAccess.createEnumeration();
                while (xEnumeration.hasMoreElements()) {
                    try {
                        System.out.println(action + " [" + unoURL + "] Found " + xEnumeration.nextElement());
                        /*
                        Any any = (Any) xEnumeration.nextElement();
                        xComponent = (XComponent) any.getObject();
                        System.out.println("Closing " + xComponent.toString());
                        close(xComponent);
                        */
                    } catch (NoSuchElementException e) {
                        // ignore
                    } catch (WrappedTargetException e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

}
