/*
 * odisee
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 05.08.12 16:57
 */

package eu.artofcoding.odisee.server;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XRefreshable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sun.star.uno.UnoRuntime.queryInterface;

public class OfficeDocument {

    private static final Logger logger = Logger.getLogger(OfficeDocument.class.getName());

    static {
        logger.setLevel(Level.OFF);
    }

    /**
     * The connection to use for dealing with the document.
     */
    private final OfficeConnection officeConnection;

    /**
     * Type of this document.
     */
    private OfficeDocumentType officeDocumentType;

    /**
     * The document.
     */
    private XComponent xComponent;

    /**
     * Constructor.
     * @param officeConnection A previously created connection to an running Office.
     */
    public OfficeDocument(OfficeConnection officeConnection) {
        this.officeConnection = officeConnection;
    }

    /**
     * Get type of this document.
     * @return OfficeDocumentType
     */
    public OfficeDocumentType getOfficeDocumentType() {
        return officeDocumentType;
    }

    /**
     * Create a new document by type.
     * @param officeDocumentType Type of document.
     * @return A newly created XComponent.
     * @throws OdiseeServerException
     */
    public XComponent newDocument(OfficeDocumentType officeDocumentType) throws OdiseeServerException {
        logger.info("officeDocumentType=" + officeDocumentType);
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerRuntimeException("Not initialized");
        }
        // URL
        String loadUrl = "private:factory/" + officeDocumentType.getInternalType();
        // Empty properties
        PropertyValue[] loadProps = new PropertyValue[0];
        try {
            // Create new document
            xComponent = officeConnection.getXComponentLoader().loadComponentFromURL(loadUrl, "_blank", 0, loadProps);
            // Remember officeDocumentType
            this.officeDocumentType = officeDocumentType;
            // Return XComponent
            return xComponent;
        } catch (com.sun.star.io.IOException e) {
            throw new OdiseeServerException(e);
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException(e);
        }
    }

    /**
     * Create a new document (from a template) by opening an existing one: document or a template.
     * To create an empty document use {@link #newDocument(OfficeDocumentType)}.
     * @param url URL to create the new document from.
     * @return A newly created XComponent.
     * @throws OdiseeServerException
     */
    public XComponent newDocument(URL url) throws OdiseeServerException {
        logger.info("newDocument(URL=" + url + ")");
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        // Define load properties according to com.sun.star.document.MediaDescriptor
        // The boolean property AsTemplate tells the office to create a new document from the given file
        List<PropertyValue> loadProps = new ArrayList<PropertyValue>();
        PropertyValue p;
        // Open as template?
        if (url.toString().matches(OdiseeConstant.NATIVE_TEMPLATE_REGEX)) {
            logger.info("Set AsTemplate for url=" + url);
            p = new PropertyValue();
            p.Name = "AsTemplate";
            p.Value = Boolean.TRUE;
            loadProps.add(p);
        }
        // TODO ODISEE_DEBUG
        p = new PropertyValue();
        p.Name = "Hidden";
        p.Value = Boolean.FALSE;
        loadProps.add(p);
        // Always execute macros
        p = new PropertyValue();
        p.Name = "MacroExecutionMode";
        p.Value = com.sun.star.document.MacroExecMode.ALWAYS_EXECUTE_NO_WARN;
        loadProps.add(p);
        // Load template
        try {
            // Check URL
            StringBuilder templateUrl = new StringBuilder();
            // Correct file: URL
            if (url.getProtocol().equals("file")) {
                File file = new File(url.toURI());
                templateUrl.append("file:///").append(file.getCanonicalPath().replace('\\', '/'));
            } else {
                templateUrl.append(url.toString());
            }
            logger.info("newDocument(URL=" + url + ") templateUrl=" + templateUrl.toString());
            // Open template
            PropertyValue[] propertyValues = loadProps.toArray(new PropertyValue[loadProps.size()]);
            xComponent = officeConnection.getXComponentLoader().loadComponentFromURL(templateUrl.toString(), "_blank", 0, propertyValues);
            // Remember document type
            int len = templateUrl.length();
            this.officeDocumentType = OfficeDocumentType.find(templateUrl.substring(len - 3));
            // Return XComponent
            return xComponent;
        } catch (URISyntaxException e) {
            throw new OdiseeServerException(e);
        } catch (java.io.IOException e) {
            throw new OdiseeServerException(e);
        } catch (com.sun.star.io.IOException e) {
            throw new OdiseeServerException(e);
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new OdiseeServerException(e);
        }
    }

    /**
     * Convenience method for {@link #newDocument(java.net.URL)} using a File reference.
     * @param file File object.
     * @return A document.
     * @throws OdiseeServerException
     */
    public XComponent newDocument(File file) throws OdiseeServerException {
        try {
            return newDocument(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new OdiseeServerException("Cannot create document from template due to malformed URL", e);
        }
    }

    public XComponent getXComponent() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        return xComponent;
    }

    public XController getXController() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        XController xController = queryInterface(XController.class, xComponent);
        if (null == xController) {
            throw new OdiseeServerException("No XController available");
        }
        return xController;
    }

    public XFrame getXFrame() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        XFrame xFrame = null;
        XController xController = queryInterface(XController.class, xComponent);
        if (null != xController) {
            if (xController.suspend(true)) {
                xFrame = xController.getFrame();
                if (null == xFrame) {
                    throw new OdiseeServerException("There is no XFrame");
                }
            }
        }
        return xFrame;
    }

    public void refreshAll() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        refreshTextFields();
        refreshDocument();
    }

    public void refreshTextFields() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        // Get textfields supplier
        XTextFieldsSupplier xTextFieldsSupplier = queryInterface(XTextFieldsSupplier.class, xComponent);
        XEnumerationAccess xEnumeratedFields = xTextFieldsSupplier.getTextFields();
        XRefreshable xRefreshable = queryInterface(XRefreshable.class, xEnumeratedFields);
        xRefreshable.refresh();
    }

    public void refreshDocument() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        XRefreshable xRefresh = queryInterface(XRefreshable.class, xComponent);
        xRefresh.refresh();
    }

    public void save() throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        // Save
        try {
            XStorable xStorable = queryInterface(XStorable.class, xComponent);
            xStorable.store();
        } catch (IOException e) {
            throw new OdiseeServerException("Cannot save document", e);
        }
    }

    public void saveAs(URL url) throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        StringBuilder builder = new StringBuilder();
        if (url.getProtocol().equals("file")) {
            try {
                File file = new File(url.toURI());
                builder.append("file:///").append(file.getCanonicalPath().replace('\\', '/'));
            } catch (URISyntaxException e) {
                throw new OdiseeServerException("Cannot convert file: URL to save document as " + url.toString(), e);
            } catch (java.io.IOException e) {
                throw new OdiseeServerException("Cannot convert file: URL to save document as " + url.toString(), e);
            }
        } else {
            builder.append(url.toString());
        }
        String saveAsUrl = builder.toString();
        // Properties
        List<PropertyValue> saveProps = new ArrayList<PropertyValue>();
        PropertyValue p;
        // When not saving in native format apply filter, e.g. export as PDF
        if (!saveAsUrl.matches(OdiseeConstant.NATIVE_DOCUMENT_REGEX)) {
            p = new PropertyValue();
            p.Name = "FilterName";
            p.Value = officeDocumentType.getPdfExportFilter();
            saveProps.add(p);
        }
        // Save as
        try {
            XStorable xStorable = queryInterface(XStorable.class, xComponent);
            PropertyValue[] propertyValues = saveProps.toArray(new PropertyValue[saveProps.size()]);
            xStorable.storeToURL(saveAsUrl, propertyValues);
        } catch (IOException e) {
            throw new OdiseeServerException("Cannot save document as " + saveAsUrl, e);
        }
    }

    public boolean setModified(boolean modified) throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        boolean bModified = false;
        // Check supported functionality of the document (model or controller).
        XModel xModel = queryInterface(XModel.class, xComponent);
        if (null != xModel) {
            XModifiable xModify = queryInterface(XModifiable.class, xModel);
            try {
                xModify.setModified(modified);
            } catch (PropertyVetoException e) {
                // Can be thrown by "setModified()" call on model. It disagree with our request.
                // But there is nothing to do then. Following "dispose()" call wasn't never called (because we catch it before).
                throw new OdiseeServerException("Document disagreed to be " + (modified ? "" : "un") + "modified", e);
            }
            bModified = true;
        }
        return bModified;
    }

    /**
     * Close this document.
     * @param force Should we close the document even when having unsaved changes?
     * @return Boolean to indicate closing was successful.
     * @throws OdiseeServerException
     */
    public boolean closeDocument(boolean force) throws OdiseeServerException {
        // Check state
        if (!officeConnection.initializationCompleted()) {
            throw new OdiseeServerException("Not initialized");
        }
        boolean bClosed = false;
        try {
            // Set modified flag in case we should forcibly close the document
            if (force) {
                setModified(false);
            }
            //
            // Try 1: Close document though XModel/XCloseable
            // Get XModel
            XModel xModel = queryInterface(XModel.class, xComponent);
            if (null != xModel) {
                XCloseable xCloseable = queryInterface(XCloseable.class, xModel);
                try {
                    xCloseable.close(true);
                    // Calling xModel.dispose(); results in com.sun.star.lang.DisposedException
                    bClosed = true;
                } catch (CloseVetoException e) {
                    bClosed = false;
                }
            }
            //
            if (!bClosed) {
                // Try 2: Close document though XFrame/XCloseable
                // Get XFrame
                XFrame xFrame = getXFrame();
                if (null != xFrame) {
                    // First try the new way: use new interface XCloseable
                    // It replaced the deprecated XTask::close() and should be preferred ... if it can be queried.
                    XCloseable xCloseable = queryInterface(XCloseable.class, xFrame);
                    if (xCloseable != null) {
                        // We deliver the owner ship of this frame not to the (possible) source which throw a CloseVetoException.
                        // We whishto have it under our own control.
                        try {
                            xCloseable.close(false);
                            bClosed = true;
                        } catch (CloseVetoException e) {
                            bClosed = false;
                        }
                    }
                }
            }
            //
            if (!bClosed) {
                // Try 3: Close document though XController/XCloseable
                try {
                    // It's a document which supports a controller .. or may by a pure window only.
                    // If it's at least a controller - we can try to suspend it. But - it can disagree with that!
                    XController xController = queryInterface(XController.class, xComponent);
                    if (xController != null) {
                        if (xController.suspend(true)) {
                            // Note: Don't dispose the controller - destroy the frame to make it right!
                            // Get XFrame
                            XFrame xFrame = getXFrame();
                            if (null != xFrame) {
                                xFrame.dispose();
                                bClosed = true;
                            }
                        }
                    }
                } catch (DisposedException e) {
                    // If an UNO object was already disposed before - he throw this special runtime exception.
                    // Of course every UNO call must be look for that - but it's a question of error handling.
                    // For demonstration this exception is handled here.
                    throw new OdiseeServerException(e);
                } catch (com.sun.star.uno.RuntimeException e) {
                    // Every UNO call can throw that.
                    // Do nothing - closing failed - that's it.
                    throw new OdiseeServerException(e);
                }
            }
        } catch (com.sun.star.uno.RuntimeException e) {
            throw new OdiseeServerException("Could not close document", e);
        }
        /*
        XComponent xComponent = officeDocument.getXComponent();
        XTextDocument xTextDocument = queryInterface(XTextDocument.class, xComponent);
        xTextDocument.dispose();
        */
        // Return
        return bClosed;
    }

}
