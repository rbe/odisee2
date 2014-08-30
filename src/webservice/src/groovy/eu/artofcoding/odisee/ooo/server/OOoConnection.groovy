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
package eu.artofcoding.odisee.ooo.server

import com.sun.star.beans.XPropertySet
import com.sun.star.bridge.BridgeExistsException
import com.sun.star.bridge.XBridge
import com.sun.star.bridge.XBridgeFactory
import com.sun.star.comp.bridgefactory.BridgeFactory
import com.sun.star.comp.helper.Bootstrap
import com.sun.star.connection.XConnection
import com.sun.star.connection.XConnector
import com.sun.star.frame.XComponentLoader
import com.sun.star.frame.XDesktop
import com.sun.star.lang.EventObject
import com.sun.star.lang.XComponent
import com.sun.star.lang.XEventListener
import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseePath
import eu.artofcoding.odisee.ooo.UnoCategory

import static eu.artofcoding.odisee.server.OdiseeConstant.TIMEOUT_1SEC
import static eu.artofcoding.odisee.server.OdiseeConstant.UNUSABLE_TIMEOUT

/**
 * Connect to an OpenOffice.org instance.
 */
class OOoConnection implements XEventListener {

    /**
     * Group this connection belongs to.
     */
    String group

    /**
     * OpenOffice instance.
     */
    OOoProcess oooProcess

    /**
     * The connection is unusable since (timestamp; milliseconds).
     */
    long unusableSince = 0

    /**
     * Wait for some time if this connection was unusable.
     */
    private int timeout

    /**
     * Execute code when connection is disposed.
     */
    private Closure disposeClosure

    /**
     * Counter for a bridge.
     */
    private static int bridgeCnt

    XComponentContext xOfficeComponentContext
    //com.sun.star.comp.servicemanager.ServiceManager
    def xServiceManager
    BridgeFactory xBridgeFactory
    XMultiComponentFactory xMultiComponentFactory
    XBridge xBridge
    XDesktop xDesktop
    XComponentLoader xComponentLoader

    /**
     * Clear references; connection has to be re-established.
     */
    private void clearReferences() {
        [
                'xOfficeComponentContext',
                'xServiceManager',
                'xMultiComponentFactory',
                'xBridgeFactory',
                'xBridge',
                'xDesktop',
                'xComponentLoader'
        ].each {
            if (OdiseePath.ODISEE_DEBUG) {
                println "${this}.clearReferences Clearing reference to ${it}"
            }
            this[it] = null
        }
    }

    /**
     * Connect to a local or remote OpenOffice instance.
     * MessageDispatcher: http://www.oooforum.org/forum/viewtopic.phtml?t=35602
     */
    private void xConnect() {
        // Close old connection
        close()
        //
        use(UnoCategory) {
            XComponentContext xRemoteContext = Bootstrap.createInitialComponentContext(null)
            Object connector = xRemoteContext.serviceManager.createInstanceWithContext('com.sun.star.connection.Connector', xRemoteContext)
            ////XConnection connection = ((XConnector) connector.uno(XConnector)).connect(oooProcess.getParsedUnoURL().getConnectionAndParametersAsString())
            XConnection connection = ((XConnector) connector.uno(XConnector)).connect(oooProcess.parsedUnoURL.connectionAndParametersAsString)
            Object bridgeFactory = xRemoteContext.serviceManager.createInstanceWithContext('com.sun.star.bridge.BridgeFactory', xRemoteContext)
            XBridgeFactory xBridgeFactory = (XBridgeFactory) bridgeFactory.uno(XBridgeFactory)
            assert null != xBridgeFactory: "${this}.xConnect: XBridgeFactory is null"
            // This is the bridge that you will dispose
            String bridgeName = "bridge${bridgeCnt++}" as String
            xBridge = xBridgeFactory.createBridge(bridgeName, 'urp', connection, null)
            if (OdiseePath.ODISEE_DEBUG) {
                println "${this}.xConnect: Created XBridge ${bridgeName}, xBridge=${xBridge?.dump()}"
            }
            // Add this as event listener
            XComponent xComponent = (XComponent) xBridge.uno(XComponent)
            xComponent.addEventListener(this)
            // Get the remote instance
            xServiceManager = xBridge.getInstance('StarOffice.ServiceManager')
            ////println "${this}.xConnect: xServiceManager=${xServiceManager?.dump()}"
            // Query the initial object for its main factory interface
            xMultiComponentFactory = (XMultiComponentFactory) xServiceManager.uno(XMultiComponentFactory)
            // retrieve the component context (it"s not yet exported from the office)
            // Query for the XPropertySet interface.
            XPropertySet xProperySet = (XPropertySet) xMultiComponentFactory.uno(XPropertySet)
            // Get the default context from the office server.
            Object oDefaultContext = xProperySet.getPropertyValue('DefaultContext')
            // Query for the interface XComponentContext.
            xOfficeComponentContext = (XComponentContext) oDefaultContext.uno(XComponentContext)
            // Now create the desktop service
            // NOTE: use the office component context here !
            Object oDesktop = xMultiComponentFactory.createInstanceWithContext('com.sun.star.frame.Desktop', xOfficeComponentContext)
            xDesktop = (XDesktop) oDesktop.uno(XDesktop)
            xComponentLoader = (XComponentLoader) oDesktop.uno(XComponentLoader)
        }
    }

    /**
     * Connect to an OpenOffice instance.
     */
    void connect() {
        // Wait some time? Maybe this connection was unusable.
        if (isUsable()) {
            // Connection already established and usable?
            // This would be reset though XEventListener#dispoing(Event)
            if (xOfficeComponentContext) {
                if (OdiseePath.ODISEE_DEBUG) {
                    println "${this}.connect: ${oooProcess.unoURL} still alive"
                }
                return
            } else {
                // Connect to the instance
                try {
                    try {
                        xConnect()
                        if (OdiseePath.ODISEE_DEBUG) {
                            println "${this}.connect: Checking connection to OOo instance at ${oooProcess.unoURL}"
                        }
                        if (null == xBridge) {
                            throw new OdiseeException('ODI-xxxx: No connection (XBridge)')
                        }
                        if (null == xComponentLoader) {
                            throw new OdiseeException('ODI-xxxx: No connection (XComponentLoader)')
                        }
                        if (OdiseePath.ODISEE_DEBUG) {
                            println "${this}.connect: Successfully connected to OOo instance at ${oooProcess.unoURL}"
                        }
                        markConnectionAsUsable()
                    } catch (BridgeExistsException e) {
                        // Connection is reused; the bridge may exist already, assume it's ok
                        if (OdiseePath.ODISEE_DEBUG) {
                            println "${this}.connect/catch: XBridge exists: ${e.message}"
                        }
                    } finally {
                        if (OdiseePath.ODISEE_DEBUG) {
                            println "${this}.connect/finally: XBridge=${xBridge} xOfficeComponentContext=${xOfficeComponentContext} xComponentLoader=${xComponentLoader}"
                        }
                    }
                } catch (e) {
                    markConnectionAsUnusable()
                    throw new OdiseeException("ODI-xxxx: Cannot connect to ${oooProcess.unoURL}, marked as unusable for ${timeout} seconds", e)
                }
            }
        } else {
            throw new OdiseeException("Connection to ${oooProcess.unoURL} sleeping for additional ${diff}")
        }
    }

    /**
     * Mark connection as unusable.
     */
    private void markConnectionAsUnusable() {
        // Set unusable-since-timestamp to 'now'
        unusableSince = System.currentTimeMillis()
        // Don't use this connection for 30 secs
        timeout = UNUSABLE_TIMEOUT
        // Clear references to OOo objects
        clearReferences()
    }

    /**
     * Mark connection as usable.
     */
    private void markConnectionAsUsable() {
        // Unset unusable-since-timestamp
        unusableSince = 0
        /*
        // Don't use this connection for 1 sec
        timeout = OdiseeConstant.TIMEOUT_1SEC
        */
    }

    /**
     * Check if this connection is usable.
     * @return
     */
    private boolean isUsable() {
        boolean usable = false
        if (unusableSince > 0) {
            long reactivateAt = unusableSince + timeout
            long curr = System.currentTimeMillis()
            long diff = reactivateAt - curr
            if (curr < reactivateAt) {
                if (OdiseePath.ODISEE_DEBUG) {
                    println "${this}.connect: ${oooProcess.unoURL}: connection sleeping for ${diff} ms"
                }
                throw new OdiseeException("Connection to ${oooProcess.unoURL} sleeping for additional ${diff}")
            } else {
                if (OdiseePath.ODISEE_DEBUG) {
                    println "${this}.connect: ${oooProcess.unoURL} curr=${curr} unusableSince=${unusableSince} reactivateAt=${reactivateAt} => ${curr < reactivateAt}"
                }
                usable = true
            }
        } else {
            usable = true
        }
        return usable
    }

    /**
     * Release connection, it is not used anymore.
     * @return
     */
    void release() {
        // Set unusableSince to give OOo instance chance to recover
        unusableSince = System.currentTimeMillis()
        // Don't use this connection for 1 sec
        timeout = TIMEOUT_1SEC
    }

    /**
     * Connection disposing.
     * @param event
     */
    @Override
    void disposing(EventObject event) {
        println "${this}.disposing: ${event}"
        // Clear references to OOo objects
        clearReferences()
        // Execute closure on dispose
        if (disposeClosure) {
            disposeClosure()
        }
    }

    /**
     * Close the connection.
     * @return Boolean Connection was closed or not
     */
    boolean close() {
        if (xBridge) {
            if (OdiseePath.ODISEE_DEBUG) {
                println "${this}.close: via XBridge/com.sun.star.lang.XComponent#dispose"
            }
            use(UnoCategory) {
                XComponent xComponent = (XComponent) xBridge.uno(XComponent)
                xComponent.dispose()
            }
            if (OdiseePath.ODISEE_DEBUG) {
                println "${this}.close: via XBridge/com.sun.star.lang.XComponent#dispose... done"
            }
            return true
        } else {
            // Call terminate yourself
            return false
        }
    }

    /**
     * Terminate the connection.
     */
    void terminate() {
        if (OdiseePath.ODISEE_DEBUG) {
            println "${this}.termiante(): Trying to terminate"
        }
        try {
            xDesktop.terminate()
        } catch (e) {
            println "${this}.terminate: Terminating connection was unsucessfull: ${e}"
        }
        // Stop process
        oooProcess?.stop()
    }

}
