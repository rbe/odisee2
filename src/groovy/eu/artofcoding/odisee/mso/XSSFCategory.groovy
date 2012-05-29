/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.mso

import eu.artofcoding.odisee.helper.Coordinate as Coord

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * This category reads and writes Excel files. It uses the Apache POI library and is capable
 * of using HSSF, XSSF and SXSSF objects. Files containing a huge number of rows are supported.
 * By default this category will use SXSSF, unless a value of zero is supplied for the
 * 'sxssfWindowSize' argument in the call to #open(File, int).
 */
class XSSFCategory {

    /**
     * Get cell type depening on class of value.
     * @param value A value.
     * @return int See org.apache.poi.ss.usermodel.Cell.
     */
    static int guessCellType(value) {
        int ret = Cell.CELL_TYPE_BLANK
        try {
            switch (value.getClass()) {
                case { it instanceof org.apache.poi.ss.usermodel.Cell }:
                    value.cellType
                    break
                case { it instanceof java.lang.String || it instanceof groovy.lang.GString }:
                    if (null != value && value[0] == "=") {
                        Cell.CELL_TYPE_FORMULA
                    } else {
                        Cell.CELL_TYPE_STRING
                    }
                    break
                case { it instanceof java.lang.Number }:
                    Cell.CELL_TYPE_NUMERIC
                    break
                case java.lang.Boolean:
                    Cell.CELL_TYPE_BOOLEAN
                    break
                default:
                    Cell.CELL_TYPE_BLANK
            }
        } catch (Exception e) {
            println "${this}.guesllCellType(${value}): EXCEPTION: ${e}"
            // ignore
        }
        return ret
    }

    /**
     * Opens an existing or creates a new spreadsheet document.
     * @param self java.io.File instance.
     * @return An instance of org.apache.poi.ss.usermodel.Workbook.
     */
    static Workbook open(java.io.File self, int sxssfWindowSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE) {
        Workbook workbook
        // Determine type of file using its extension
        def guessType = { fileOrName ->
            // Get name of file
            String name
            if (fileOrName instanceof java.io.File) {
                name = fileOrName.name
            } else {
                name = fileOrName
            }
            // Determine type
            Class type
            switch (name) {
                case ~/.*.xls$/:
                    type = HSSFWorkbook
                    break
                case ~/.*.xlsx$/:
                    // Use (S)XSSF?
                    type = sxssfWindowSize > 0 ? SXSSFWorkbook : XSSFWorkbook
                    break
                default:
                    throw new IllegalStateException("Unknown file type (xls and xlsx are supported)")
            }
            type
        }
        // If the file does not exist or is empty, create a new one
        if (!self.exists() || self.length() == 0) {
            Class type = guessType(self)
            // Use (S)XSSF?
            workbook = sxssfWindowSize > 0 ? type.newInstance(sxssfWindowSize) : type.newInstance()
        }
        // or read contents of existing file
        else {
            Class type = guessType(self)
            workbook = self.withInputStream { fin ->
                type.newInstance(fin)
            }
        }
        workbook
    }

    /**
     * Get a certain sheet from an Excel file/Workbook.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     */
    static Sheet getSheet(java.io.File self, Workbook workbook, sheet) {
        Sheet _sheet
        switch (sheet.getClass()) {
            case { it in [HSSFSheet, XSSFSheet, SXSSFSheet] }:
                _sheet = sheet
                break
            case java.lang.String:
                _sheet = workbook.getSheet(sheet)
                break
            case java.lang.Integer:
                _sheet = workbook.getSheetAt(sheet)
                break
            default:
                throw new IllegalStateException("Unknown sheet type: ${sheet}")
        }
        if (null == _sheet) {
            throw new IllegalStateException("No sheet?")
        }
        _sheet
    }

    /**
     * Get one or all org.apache.poi.ss.usermodel.Row from a sheet.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param row int, number of row to get, leave empty or set to -1 to get all rows.
     * @return org.apache.poi.ss.usermodel.Row or List<Row>.
     */
    static def getRow(java.io.File self, Workbook workbook, sheet, int row = -1) {
        Sheet _sheet = getSheet(self, workbook, sheet)
        // Get a certain row
        if (row > -1) {
            _sheet.getRow(row)
        } else { // Get all rows
            def rows = []
            0.upto(_sheet.physicalNumberOfRows - 1) {
                rows << _sheet.getRow(it)
            }
            rows
        }
    }

    /**
     * Get one or all org.apache.poi.ss.usermodel.Row from a sheet as a List.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param row int, number of row to get, leave empty or set to -1 to get all rows.
     * @param closure An optional closure to execute before a cell value is returned.
     * @return List < org.apache.poi.ss.usermodel.Row > .
     */
    static List<Row> getRowAsList(java.io.File self, Workbook workbook, sheet, int row = -1, Closure before = null) {
        Sheet _sheet = getSheet(self, workbook, sheet)
        // Closure to get all cells in a certain row
        Cell _cells = { rowNum ->
            Row _row = _sheet.getRow(rowNum)
            def cellsInRow = []
            0.upto(_row.physicalNumberOfCells - 1) { cellNum ->
                cellsInRow << getCellValue(self, workbook, _sheet, rowNum, cellNum, before)
            }
            cellsInRow
        }
        // Get a certain row
        if (row > -1) {
            _cells(row)
        } else { // Get all rows
            def rows = []
            0.upto(_sheet.physicalNumberOfRows - 1) { rowNum ->
                rows << _cells(rowNum)
            }
            rows
        }
    }

    /**
     * Get one or all org.apache.poi.ss.usermodel.Row from a sheet as a Map.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param row int, number of row to get, leave empty or set to -1 to get all rows.
     * @param closure An optional closure to execute before a cell value is returned.
     * @return Map < String , org.apache.poi.ss.usermodel.Row > .
     */
    static Map getRowAsMap(java.io.File self, Workbook workbook, sheet, int row = -1, Closure before = null) {
        Sheet _sheet = getSheet(self, workbook, sheet)
        def sheetName = workbook.getSheetName(workbook.getSheetIndex(_sheet))
        // Closure to get all cells in a certain row
        Cell _cells = { rowNum ->
            def map = [:]
            Row _row = _sheet.getRow(rowNum)
            Cell _cell
            0.upto(_row.physicalNumberOfCells - 1) { cellNum ->
                _cell = _row.getCell(cellNum)
                def coord = "${sheetName}\$${((char) ((int) 'A') + _cell.columnIndex)}${(_row.rowNum + 1)}"
                map[coord] = getCellValue(self, workbook, _sheet, rowNum, cellNum)
            }
            map
        }
        // Get a certain row
        if (row > -1) {
            _cells(row)
        } else { // Get all rows
            def map = [:]
            0.upto(_sheet.physicalNumberOfRows - 1) { rowNum ->
                map += _cells(rowNum)
            }
            map
        }
    }

    /**
     * Get one or all org.apache.poi.ss.usermodel.Cells from a row.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param row int, number of row to get.
     * @param cell int, number of cell in row to get, leave empty or set to -1 to get all cells.
     * @param closure An optional closure to execute before a cell value is returned.
     * @return org.apache.poi.ss.usermodel.Cell or List<String, org.apache.poi.ss.usermodel.Cell>.
     */
    static def getCell(java.io.File self, Workbook workbook, sheet, row, int cell = -1, Closure before = null) {
        Sheet _sheet = getSheet(self, workbook, sheet)
        // Get a certain cell
        if (cell > -1) {
            _sheet.getRow(row).getCell(cell)
        } else { // Get all cells
            Row _row = _sheet.getRow(row)
            def cells = []
            0.upto(_row.physicalNumberOfCells - 1) {
                if (before) {
                    before(aCell)
                }
                cells << _row.getCell(it)
            }
            cells
        }
    }

    /**
     * Get value of one or all org.apache.poi.ss.usermodel.Cells from a row.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param row int, number of row to get.
     * @param cell int, number of cell in row to get, leave empty or set to -1 to get all cells.
     * @param closure An optional closure to execute before a cell value is returned.
     * @return org.apache.poi.ss.usermodel.Cell or List<String, org.apache.poi.ss.usermodel.Cell>.
     */
    static def getCellValue(java.io.File self, Workbook workbook, sheet, row, int cell = -1, Closure before = null) {
        Cell _cell = getCell(self, workbook, sheet, row, cell)
        def val = { aCell ->
            // Execute closure before getting cell value, can be used to re-format a cell
            // Example:
            //    Big numbers in Excel files may be represented in scientific format like "12345E14"
            //    Define a closure like { aCell -> aCell.cellType = Cell.CELL_TYPE_STRING }
            //    to reformat the cell as string
            if (before) {
                before(aCell)
            }
            switch (aCell.cellType) {
                case Cell.CELL_TYPE_STRING:
                    aCell.stringCellValue
                    break
                case Cell.CELL_TYPE_NUMERIC:
                    aCell.numericCellValue
                    break
            }
        }
        if (_cell instanceof List) {
            _cell.each { val(it) }
        } else {
            val(_cell)
        }
    }

    /**
     * Get statistics about an Excel file.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     */
    static Map getStatistics(java.io.File self, Workbook workbook) {
        // Row count
        def rowsPerSheet = [:]
        Integer totalRowCount = 0
        if (workbook.numberOfSheets > 0) {
            0.upto(workbook.numberOfSheets - 1) { i ->
                Integer rows = workbook.getSheetAt(i).physicalNumberOfRows
                rowsPerSheet[workbook.getSheetName(i)] = rows
                totalRowCount += rows
            }
        }
        // Create map with all statistics
        [
                numberOfSheets: workbook.numberOfSheets,
                rowsPerSheet: rowsPerSheet,
                totalRowCount: totalRowCount
        ]
    }

    /**
     * Create a new (empty) sheet in a workbook.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheetName Name of sheet.
     * @param data Optional List or Map with data: Map = ['A1': 'hallo'] or list rows=[cols=[]].
     */
    static Sheet addSheet(java.io.File self, Workbook workbook, sheetName, data = null) {
        Sheet _sheet = workbook.createSheet(sheetName)
        if (data) {
            fillSheet(self, workbook, _sheet, data)
        }
        _sheet
    }

    /**
     * Fill data in an Excel workbook/sheet.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param data Map = ['A1': 'hallo'] or list rows=[cols=[]]
     */
    static Sheet fillSheet(java.io.File self, Workbook workbook, sheet, data) {
        Sheet _sheet = getSheet(self, workbook, sheet)
        Row _row
        Cell _cell
        // Map: key=coordinate, value=value
        if (data instanceof Map) {
            def coord
            data.each { k, v ->
                coord = Coord.parseCoordinate(k)
                // Do not create a row more than once, it will delete row's contents
                _row = _sheet.physicalNumberOfRows >= coord.row + 1 ? _sheet.getRow(coord.row) : _sheet.createRow(coord.row)
                _cell = _row.createCell(coord.column, guessCellType(v))
                _cell.setCellValue(v)
            }
        }
        // List rows=[cols=[]]: index=row number
        else if (data instanceof List) {
            data.eachWithIndex { row, rowIdx ->
                row.eachWithIndex { col, colIdx ->
                    // Do not create a row more than once, it will delete row's contents
                    _row = _sheet.physicalNumberOfRows >= rowIdx + 1 ? _sheet.getRow(rowIdx) : _sheet.createRow(rowIdx)
                    _cell = _row.createCell(colIdx, guessCellType(data[rowIdx][colIdx]))
                    _cell.setCellValue(data[rowIdx][colIdx] ?: '')
                }
            }
        }
        // Return sheet
        _sheet
    }

    /**
     * Fill data in an Excel workbook/sheet.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param startingRow Row to start adding rows.
     * @param sheet A sheet as an instance of org.apache.poi.ss.usermodel.Sheet, by name or by index in the workbook.
     * @param data List: rows=[cols=[]]
     */
    static Sheet addRows(java.io.File self, Workbook workbook, sheet, startingRow, data) {
        Sheet _sheet = getSheet(self, workbook, sheet)
        Row _row
        Cell _cell
        if (data instanceof List) {
            data.eachWithIndex { row, rowIdx ->
                int theRow = startingRow + rowIdx + 1
                row.eachWithIndex { col, colIdx ->
                    // Do not create a row more than once, it will delete row's contents
                    _row = _sheet.physicalNumberOfRows >= theRow ? _sheet.getRow(theRow) : _sheet.createRow(theRow)
                    _cell = _row.createCell(colIdx, guessCellType(data[rowIdx][colIdx]))
                    //String address = new CellReference(_cell).formatAsString();
                    _cell.setCellValue(data[rowIdx][colIdx] ?: '')
                }
            }
        }
        // Return sheet
        _sheet
    }

    /**
     * Save an Excel file.
     * @param self Instance of java.io.File.
     * @param workbook An instance of org.apache.poi.ss.usermodel.Workbook.
     * @param toFile Optional other instance of java.io.File to save workbook to.
     */
    static java.io.File save(java.io.File self, Workbook workbook, File toFile = null) {
        // Where to save this workbook?
        java.io.File f
        if (!toFile) {
            f = self
        } else {
            f = toFile instanceof java.io.File ? toFile : new java.io.File(toFile)
        }
        // Write file
        f.withOutputStream { fout ->
            workbook.write(fout)
        }
        // No file?
        if (!f) {
            throw new IllegalStateException("No output file")
        }
        // Return file
        f
    }

}
