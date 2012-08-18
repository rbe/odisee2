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

/**
 * Writer Autotexts.
 */
class OOoAutotextCategory {

    /**
     * Get an autotext.
     */
    static com.sun.star.text.XAutoTextEntry getAutotext(com.sun.star.lang.XComponent component, String autotextGroup, String autotext) {
        use(UnoCategory) {
            //println "${this}.getAutotext: autotextGroup=${autotextGroup} autotext=${autotext} bookmark=${bookmark} component.oooConnection=${component.oooConnection}"
            // Create service
            def oAutotextContainer = component.oooConnection.xMultiComponentFactory.createInstanceWithContext('com.sun.star.text.AutoTextContainer', component.oooConnection.xOfficeComponentContext)
            //println "${this}.getAutotext: oAutotextContainer=${oAutotextContainer}"
            // Get autotext group: com.sun.star.text.XAutoTextGroup
            def xAutotextGroup = null
            try {
                xAutotextGroup = oAutotextContainer.uno(com.sun.star.container.XNameAccess).getByName(autotextGroup)
                //println "${this}.getAutotext: xAutotextGroup=${xAutotextGroup}"
            } catch (com.sun.star.container.NoSuchElementException e) {
                println "ODI-xxxx: Could not find autotext group ${autotextGroup}"
            }
            if (xAutotextGroup) {
                try {
                    // Get autotext entry: com.sun.star.text.XAutoTextEntry
                    def theAutotext = xAutotextGroup.uno(com.sun.star.container.XNameAccess).getByName(autotext).uno(com.sun.star.text.XAutoTextEntry)
                    //println "${this}.getAutotext: theAutotext=${theAutotext}"
                    return theAutotext
                } catch (e) {
                    throw new OdiseeException("Cannot find autotext ${autotextGroup}.${autotext}", e)
                }
            }
        }
    }

    /**
     * Insert an autotext at a bookmark.
     *
     * Sub Main
     *     oDoc = ThisComponent
     *     oText = oDoc.Text
     *     oTextCursor = oText.CreateTextCursor
     *     oTextCursor.gotoEnd(False)
     *     oAutoTextContainer = getProcessServiceManager().createInstance("com.sun.star.text.AutoTextContainer")
     *     oGroup = oAutoTextContainer.getByName("standard")
     *     oEntry = oGroup.getByName("mfg")
     *     oEntry.applyTo(oTextCursor)
     * End Sub
     */
    static void insertAutotextAtBookmark(com.sun.star.lang.XComponent component, String autotextGroup, String autotext, String bookmark) {
        com.sun.star.text.XAutoTextEntry theAutotext = component.getAutotext(autotextGroup, autotext)
        if (theAutotext) {
            use(UnoCategory) {
                try {
                    // Goto bookmark and insert text
                    def bm = component.uno(com.sun.star.text.XBookmarksSupplier).bookmarks.getByName(bookmark).uno(com.sun.star.text.XTextContent)
                    //println "${this}.insertAutotextAtBookmark: bm=${bm}"
                    // Apply autotext at cursor position
                    def anchor = bm.anchor
                    theAutotext.applyTo(anchor)
                } catch (com.sun.star.container.NoSuchElementException e) {
                    println "ODI-xxxx: Could not find bookmark ${bookmark}"
                }
            }
        }
    }

    /**
     * Insert an autotext at the end of the document.
     */
    static void insertAutotextAtEnd(com.sun.star.lang.XComponent component, String autotextGroup, String autotext) {
        com.sun.star.text.XAutoTextEntry theAutotext = component.getAutotext(autotextGroup, autotext)
        if (theAutotext) {
            use(UnoCategory) {
                // Create cursor and put it at end of document
                /*com.sun.star.text.XText*/
                def sup = component.uno(com.sun.star.text.XTextDocument).text
                /*com.sun.star.text.XTextCursor*/
                def cur = sup.createTextCursor()
                cur.gotoEnd(false)
                // Apply autotext at cursor position
                theAutotext.applyTo(cur)
            }
        }
    }

}
