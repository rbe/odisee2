/*
 * odisee
 * webservice
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 03.11.12 14:06
 */
package eu.artofcoding.odisee.ooo

/**
 * Writer Bookmarks.
 */
class OOoBookmarkCategory {

    /**
     * Insert a graphic at a bookmark.
     */
    def static insertGraphicAtBookmark(com.sun.star.lang.XComponent component, String bookmark, java.io.File url) {
        component.executeOdiseeMacro("Graphic.insertGraphicAtBookmarkUsingDispatcher", [bookmark, url])
    }

    /**
     * Insert some text at a bookmark.
     */
    def static setTextAtBookmark(com.sun.star.lang.XComponent component, String bookmark, String text) {
        use(UnoCategory) {
            def bm = component.uno(com.sun.star.text.XBookmarksSupplier).getBookmarks().getByName(bookmark)
            bm.uno(com.sun.star.text.XTextContent).getAnchor().setString(text)
            // TODO Throws com.sun.star.lang.DisposedException:
            // component.executeOdiseeMacro("WriterBookmark.setTextAtBookmarkThis2", [bookmark, text])
        }
    }

    /**
     * Get text/a paragraph from a bookmark.
     */
    def static getParagraphAtBookmark(com.sun.star.lang.XComponent component, String bookmark) {
        use(UnoCategory) {
            def bm = component.uno(com.sun.star.text.XBookmarksSupplier).getBookmarks().getByName(bookmark)
            bm.uno(com.sun.star.text.XTextContent).getAnchor().getString()
            // TODO Throws com.sun.star.lang.DisposedException:
            // component.executeOdiseeMacro("WriterBookmark.getParagraphAtBookmarkThis", [bookmark])
        }
    }

    /**
     *
     */
    def static get(com.sun.star.lang.XComponent component, String name) {
        component.getParagraphAtBookmark(name)
    }

    /**
     * Set text at bookmark.
     */
    def static set(com.sun.star.lang.XComponent component, String name, Object value) {
        component.setTextAtBookmark(name, value)
    }

}
