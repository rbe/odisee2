/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.ooo

import eu.artofcoding.odisee.OdiseeException
import eu.artofcoding.odisee.OdiseeFileFormat
import eu.artofcoding.odisee.helper.Profile
import eu.artofcoding.odisee.ooo.server.OOoConnection
import com.sun.star.lang.XComponent
import com.sun.star.util.CloseVetoException
import com.sun.star.util.XCloseable
import com.sun.star.frame.XModel
import com.sun.star.frame.XDispatchHelper
import com.sun.star.frame.XStorable
import com.sun.star.util.XRefreshable
import com.sun.star.frame.XController
import com.sun.star.frame.XFrame
import com.sun.star.frame.XDispatchProvider
import com.sun.star.script.provider.XScriptProviderSupplier
import com.sun.star.script.provider.XScript
import eu.artofcoding.odisee.OdiseePath
import eu.artofcoding.odisee.server.OfficeConnection

/**
 * Things we can do with any OpenOffice.org document.
 */
class OOoDocumentCategory {

    /**
     * Standard properties for opening a document.
     */
    public static final Map STD_LOAD_PROP = [
            Hidden: Boolean.TRUE,
            MacroExecutionMode: 'com.sun.star.document.MacroExecMode.ALWAYS_EXECUTE_NO_WARN'
    ]

    /**
     * Standard properties for saving a document.
     */
    public static final Map STD_SAVE_PROP = [
            Overwrite: Boolean.TRUE
    ]

    private static final String PRIVATE_URL_REGEX = /private.*/
    private static final String TEMPLATE_EXT_REGEX = /ot[gpst]/
    private static final List<Integer> TWO_ZERO = [0, 0]
    private static final List TWO_NULL = [null, null]

    /**
     * Macro URL with vnd.sun.star.script:xxx?opt=value
     */
    private static final String MACRO_URL_W_OPT = /.*\?.*/

    /**
     * Ensure proper file URL.
     */
    static String getFileURL(File file) {
        'file:///' + ((file.toURL() as String) - 'file:')
    }

    /**
     * Make a PropertyValue object.
     */
    static com.sun.star.beans.PropertyValue makePropertyValue(Object name, Object value) {
        new com.sun.star.beans.PropertyValue(Name: name as String, Value: value)
    }

    /**
     * Open an OpenOffice.org document.
     */
    //static XComponent open(File file, OOoConnection oooConnection, props = null) {
    static XComponent open(File file, OfficeConnection oooConnection, props = null) {
        // Add connection to template's File object
        file.metaClass._oooConnection = oooConnection
        file.metaClass.getOooConnection = {-> _oooConnection }
        // Properties for loading component
        List properties = []
        // Is this a template?
        if (file.name.length() > 3 && file.name.toLowerCase()[-3..-1] ==~ TEMPLATE_EXT_REGEX) {
            // Check if template exists
            if (!file.exists()) {
                throw new OdiseeException("Template ${file} not found")
            }
            // Set property for loading
            properties << makePropertyValue('AsTemplate', Boolean.TRUE)
        }
        // Copy given properties
        props?.each { k, v ->
            properties << makePropertyValue(k as String, v)
        }
        String sURL = null
        // If we got a regular file and no private: URL
        if (file ==~ PRIVATE_URL_REGEX) {
            // URL must be private:... for this
            sURL = file.path
        } else {
            // Does the file exist? If not, create an empty file
            if (!file.exists()) {
                file.createNewFile()
            }
            // Ensure correct URL to prevent "URL seems to be an unsupported one"
            // file.toURL() would give "file:/document.odt" (missing slashes)
            sURL = getFileURL(file)
        }
        // Open document
        //if (!oooConnection.xComponentLoader) {
        if (!oooConnection.getXComponentLoader()) {
            throw new OdiseeException('No XComponentLoader!')
        }
        //XComponent xComponent = oooConnection.xComponentLoader.loadComponentFromURL(sURL, '_blank', 0, (properties ?: null) as com.sun.star.beans.PropertyValue[])
        XComponent xComponent = oooConnection.getXComponentLoader().loadComponentFromURL(sURL, '_blank', 0, (properties ?: null) as com.sun.star.beans.PropertyValue[])
        // Add OOoConnection to XComponent
        xComponent.metaClass._oooConnection = oooConnection
        xComponent.metaClass.getOooConnection = {-> _oooConnection }
        xComponent.metaClass._file = file
        xComponent.metaClass.getFile = {-> _file }
        // Return component
        xComponent
    }

    /**
     * Save an OpenOffice document.
     * @param component com.sun.star.lang.XComponent
     */
    static save(XComponent component) {
        use(UnoCategory) {
            // Refresh component
            refresh(component)
            // Refresh text fields
            use(OOoFieldCategory) {
                component.refreshTextFields()
            }
            // Get XStorable and call store()
            XStorable xStorable = (XStorable) component.uno(XStorable)
            xStorable.store()
        }
    }

    /**
     * Save an OpenOffice.org document to an alternate URL or format.
     * @param component com.sun.star.lang.XComponent
     */
    static saveAs(XComponent component, File file, props = null) {
        use(UnoCategory) {
            // Properties for saving component
            List properties = []
            // Filter for saving?
            String ext = file.name.toLowerCase()[-3..-1]
            if (!(ext ==~ /od[gpst]/)) {
                properties << makePropertyValue('FilterName', OdiseeFileFormat[ext].filter)
            }
            // Copy given properties
            props?.each { k, v ->
                properties << makePropertyValue(k, v)
            }
            /*
            // Refresh
            refresh(component)
            */
            // Refresh text fields
            use(OOoFieldCategory) {
                component.refreshTextFields()
            }
            // Save document
            String fileURL = getFileURL(file)
            try {
                XStorable xStorable = (XStorable) component.uno(XStorable)
                xStorable.storeToURL(fileURL, (properties ?: null) as com.sun.star.beans.PropertyValue[])
            } catch (e) {
                throw new OdiseeException("Could not save document at ${fileURL}", e)
            }
        }
    }

    /**
     * Close a document.
     * http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/OfficeDev/Closing_Documents
     */
    static close(XComponent component) {
        try {
            use(UnoCategory) {
                // Close through XModel
                XModel xModel = (XModel) component.uno(XModel)
                if (xModel) {
                    XCloseable xCloseable = (XCloseable) xModel.uno(XCloseable)
                    try {
                        xCloseable.close(true)
                    } catch (CloseVetoException e) {
                        // Ignore
                    }
                } else {
                    /*
                    XCloseable xCloseable = component.uno(com.sun.star.util.XCloseable)
                    xCloseable.close(true)
                    */
                    // Dispose document
                    component.dispose()
                }
            }
        } finally {
            // Remove references
            component.metaClass._oooConnection = null
            component.metaClass.getOooConnection = null
            component.metaClass._file = null
            component.metaClass.getFile = null
        }
    }

    /**
     * Refresh a document.
     */
    static refresh(XComponent component) {
        Profile.time 'OOoDocumentCategory.refresh', {
            use(UnoCategory) {
                XRefreshable xRefreshable = (XRefreshable) component.uno(XRefreshable)
                xRefreshable.refresh()
            }
        }
    }

    /**
     * Execute a dispatch.
     */
    static executeDispatch(XComponent component, String name, Map params = null) {
        Profile.time "OOoDocumentCategory.executeDispatch($name)", {
            use(UnoCategory) {
                // Get XMultiComponentFactory
                OOoConnection oooConnection = (OOoConnection) component.oooConnection
                if (oooConnection) {
                    Object o = oooConnection.xMultiComponentFactory.createInstanceWithContext('com.sun.star.frame.DispatchHelper', oooConnection.xOfficeComponentContext)
                    // XDispatchHelper
                    XDispatchHelper dispatchHelper = (XDispatchHelper) o.uno(XDispatchHelper)
                    com.sun.star.beans.PropertyValue[] props = params.collect { k, v ->
                        makePropertyValue(k, v)
                    } as com.sun.star.beans.PropertyValue[]
                    // XModel
                    XModel xModel = (XModel) component.uno(XModel)
                    // XController
                    XController xController = xModel.currentController //getCurrentController()
                    // XFrame
                    XFrame xFrame = xController.frame //getFrame()
                    // Call dispatcher
                    XDispatchProvider xDispatchProvider = (XDispatchProvider) xFrame.uno(XDispatchProvider)
                    dispatchHelper.executeDispatch(xDispatchProvider, name, '', 0, props)
                } else {
                    println "${this}.executeDispatch(${name}): No connection!"
                }
            }
        }
    }

    /**
     * Execute a macro.
     */
    static executeMacro(XComponent component, String macroName, Object[] params = []) {
        Profile.time "OOoDocumentCategory.executeMacro($macroName)", {
            def name = new StringBuilder()
            // No URL?
            if (!macroName.startsWith('vnd')) {
                name << 'vnd.sun.star.script:'
            }
            // Append macro name; no information about language and location? Append default.
            if (macroName ==~ MACRO_URL_W_OPT) {
                name << macroName
            } else {
                name << (macroName << '?language=Basic&location=document')
            }
            // Ensure a java.lang.String and replace HTML entities
            name = name.toString().replaceAll('&amp;', '&')
            if (OdiseePath.ODISEE_DEBUG) {
                println "ODI-xxxx: Processing macro '${name}'"
            }
            try {
                use(UnoCategory) {
                    XScriptProviderSupplier xScriptPS = (XScriptProviderSupplier) component.uno(XScriptProviderSupplier)
                    XScript xScript = xScriptPS.scriptProvider.getScript(name.toString())
                    short[][] outParamIndex = [TWO_ZERO, TWO_ZERO] as short[][]
                    Object[][] outParam = [TWO_NULL, TWO_NULL]/* as Object[][]*/
                    xScript.invoke(params/* as Object[]*/, outParamIndex, outParam)
                }
            } catch (e) {
                println "ODI-xxxx: Cannot execute macro: ${name}: ${e}"
            }
        }
    }

    /**
     * Execute an Odisee macro. Odisee must be installed as a shared library.
     * @param macroName Just name of Odisee Basic module and Sub/Function name. e.g. UNO.isWriter().
     */
    static executeOdiseeMacro(XComponent component, String macroName, Object[] params) {
        executeMacro(component, "Odisee.${macroName}?language=Basic&location=application", params)
    }

}
