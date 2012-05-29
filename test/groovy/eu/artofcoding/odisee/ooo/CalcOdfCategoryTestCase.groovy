/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.ooo

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument

/**
 *
 */
class CalcOdfCategoryTestCase extends GroovyTestCase {

    /**
     *
     */
    void testCreateOds() {
        // Create reference to non-existing file, category will create empty spreadsheet
        def f = new java.io.File('test/report/groovy/data', "testCreateOds_${this.getClass().name}.ods")
        f.parentFile.mkdirs()
        use(CalcOdfCategory) {
            OdfSpreadsheetDocument doc = f.open()
            f.fillSheet(doc, 0, [['A1', 'A2'], ['B1', 'B2']])
            f.save(doc)
        }
    }

}
