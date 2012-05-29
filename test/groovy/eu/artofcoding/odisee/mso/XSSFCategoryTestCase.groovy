/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.mso

import org.apache.poi.ss.usermodel.Workbook

/**
 *
 */
class XSSFCategoryTestCase extends GroovyTestCase {

    /**
     *
     */
    void testCreateHSSF() {
        def f = new java.io.File('test/report/groovy/data', "testCreateHSSF_${this.getClass().name}.xls")
        use(XSSFCategory) {
            Workbook workbook = f.open(/*sxssfWindowsSize*/ 0)
            f.addSheet(workbook, "Odisee-HSSF-Test", [["A1", "B1"], ["A2", "B2"]])
            f.save(workbook)
        }
    }

    /**
     *
     */
    void testCreateXSSF() {
        def f = new java.io.File('test/report/groovy/data', "testCreateXSSF_${this.getClass().name}.xlsx")
        use(XSSFCategory) {
            Workbook workbook = f.open(/*sxssfWindowsSize*/ 0)
            f.addSheet(workbook, "Odisee-XSSF-Test", [["A1", "B1"], ["A2", "B2"]])
            f.save(workbook)
        }
    }

    /**
     *
     */
    void testCreateSXSSF() {
        def f = new java.io.File('test/report/groovy/data', "testCreateSXSSF_${this.getClass().name}.xlsx")
        use(XSSFCategory) {
            Workbook workbook = f.open(/*sxssfWindowsSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE*/)
            f.addSheet(workbook, "Odisee-SXSSF-Test", [["A1", "B1"], ["A2", "B2"]])
            f.save(workbook)
        }
    }

}
