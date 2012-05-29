/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.ooo

import eu.artofcoding.odisee.helper.Coordinate as Coord

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument
import org.odftoolkit.odfdom.doc.table.OdfTable

/**
 * Category to allow creating and reading ODF spreadsheets/.ods files.
 */
class CalcOdfCategory {

    /**
     * Opens an existing or creates a new spreadsheet document.
     * @param self java.io.File instance.
     * @return org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument
     */
    static OdfSpreadsheetDocument open(java.io.File self) {
        OdfSpreadsheetDocument doc
        // If the file does not exist, create a new one
        if (!self.exists()) {
            doc = OdfSpreadsheetDocument.newSpreadsheetDocument()
        }
        // or read contents of existing file
        else {
            doc = OdfSpreadsheetDocument.loadDocument(self)
        }
        doc
    }

    /**
     * Get all tables in spreadsheet document.
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @return List < org.odftoolkit.odfdom.doc.table.OdfTable >
     */
    static List<OdfTable> getTables(java.io.File self, OdfSpreadsheetDocument doc) {
        doc.tableList
    }

    /**
     * Create a table (others call this a sheet).
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @param sheet A sheet as an instance of org.odftoolkit.odfdom.doc.table.OdfTable, by name or by index.
     * @param name java.lang.String, optional name for table.
     * @param rows java.lang.Integer, optional row count; defaults to 1.
     * @param columns java.lang.Integer, optional columns count; defaults to 1.
     * @return Instance of org.odftoolkit.odfdom.doc.table.OdfTable, parameter 'sheet'.
     */
    static OdfTable createTable(java.io.File self, OdfSpreadsheetDocument doc, String name = null, rows = 1, columns = 1) {
        OdfTable table = OdfTable.newTable(doc, rows, columns)
        if (name) {
            table.tableName = name
        }
        table
    }

    /**
     * Get a specific table by name or index.
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @param sheet A sheet as an instance of org.odftoolkit.odfdom.doc.table.OdfTable, by name or by index.
     * @return Instance of org.odftoolkit.odfdom.doc.table.OdfTable, parameter 'sheet'.
     */
    static OdfTable getTable(java.io.File self, OdfSpreadsheetDocument doc, sheet) {
        // Get table
        OdfTable table
        switch (sheet.getClass()) {
            case java.lang.String:
                table = doc.getTableByName(sheet)
                break
            case java.lang.Integer:
                table = getTables(self, doc)[sheet]
                break
            default:
                throw new IllegalStateException("Unknow table: ${sheet}")
        }
        if (!table) {
            throw new IllegalStateException("No table found by name or index: ${sheet}")
        }
        table
    }

    /**
     * Ensure capacity for rows and columns of a table.
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @param sheet A sheet as an instance of org.odftoolkit.odfdom.doc.table.OdfTable, by name or by index.
     * @param rows Number of rows the table should have got; 1-indexed.
     * @param columns Number of columns the table should have got; 1-indexed.
     * @return Instance of org.odftoolkit.odfdom.doc.table.OdfTable, parameter 'sheet'.
     */
    static OdfTable ensureTableCapacity(java.io.File self, OdfSpreadsheetDocument doc, sheet, rows, columns) {
        // Get the table
        OdfTable table = getTable(self, doc, sheet)
        // Enough rows?
        if (table.rowCount >= rows) {
            // Do nothing
        } else {
            def rowsToAppend = rows - table.rowCount
            //println "appending ${rowsToAppend} rows"
            table.appendRows(rowsToAppend)
        }
        // Enough columns?
        if (table.columnCount >= columns) {
            // Do nothing
        } else {
            def columnsToAppend = columns - table.columnCount
            //println "appending ${columnsToAppend} columns"
            table.appendColumns(columnsToAppend)
        }
        // Return table
        table
    }

    /**
     *
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @param sheet A sheet as an instance of org.odftoolkit.odfdom.doc.table.OdfTable, by name or by index.
     * @return Instance of org.odftoolkit.odfdom.doc.table.OdfTable, parameter 'sheet'.
     */
    static OdfTable setData(java.io.File self, OdfSpreadsheetDocument doc, sheet) {
        // Get table
        OdfTable table = getTable(self, doc, sheet)
        // Ensure capacity
        ensureTableCapacity(self, doc, sheet, 10, 10)
        // Set column value; getRowByIndex and getCellByIndex use 0-based indexes
        table.getRowByIndex(10 - 1).getCellByIndex(10 - 1).stringValue = "Hallo ODF"
        // Return table
        table
    }

    /**
     * Fill data in table/sheet.
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @param sheet A sheet as an instance of org.odftoolkit.odfdom.doc.table.OdfTable, by name or by index.
     * @param data Map = ['A1': 'hallo'] or list rows=[cols=[]]
     * @return Instance of org.odftoolkit.odfdom.doc.table.OdfTable, parameter 'sheet'.
     */
    static OdfTable fillSheet(java.io.File self, OdfSpreadsheetDocument doc, sheet, data) {
        OdfTable table = getTable(self, doc, sheet)
        def _row
        def _cell
        // Map: key=coordinate, value=value
        if (data instanceof Map) {
            def coord
            data.each { k, v ->
                // Parse coordinate
                coord = Coord.parseCoordinate(k)
                // Ensure capacity
                ensureTableCapacity(self, doc, sheet, coord.row, coord.column)
                // Set cell value
                table.getRowByIndex(coord.row).getCellByIndex(coord.column).stringValue = v
            }
        }
        // List rows=[cols=[]]: index=row number
        else if (data instanceof List) {
            data.eachWithIndex { row, rowIdx ->
                row.eachWithIndex { col, colIdx ->
                    // Ensure capacity
                    ensureTableCapacity(self, doc, sheet, rowIdx, colIdx)
                    // Set cell value
                    table.getRowByIndex(rowIdx).getCellByIndex(colIdx).stringValue = data[rowIdx][colIdx]
                }
            }
        }
        // Return table
        table
    }

    /**
     * Save and close spreadsheet document.
     * @param self java.io.File instance.
     * @param doc An instance of org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument.
     * @param toFile Alternative file to save this spreadsheet document to.
     * @return java.io.File instance.
     */
    static java.io.File save(java.io.File self, OdfSpreadsheetDocument doc, java.io.File toFile = null) {
        // Where to save this spreadsheet?
        java.io.File f
        if (!toFile) {
            f = self
        } else {
            f = toFile instanceof java.io.File ? toFile : new java.io.File(toFile)
        }
        // No file?
        if (!f) {
            throw new IllegalStateException("No output file")
        }
        // Save and close
        doc.save(f)
        doc.close()
        // Return file
        f
    }

}
