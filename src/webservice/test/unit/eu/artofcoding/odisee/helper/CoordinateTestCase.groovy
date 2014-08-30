/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.helper

import eu.artofcoding.odisee.helper.Coordinate as Coord

class CoordinateTestCase extends GroovyTestCase {

    void testCoordinate() {
        // Z3 = column #26, 0-based index 25, row #3, 0-based index 2
        assertEquals([table: null, sheet: null, column: 'Z', columnIndex: 25, row: 3, rowIndex: 2, coord: 'Z3'], Coord.'Z3')
        assertEquals([table: null, sheet: null, column: 'Z', columnIndex: 25, row: 3, rowIndex: 2, coord: '$Z$3'], Coord.'$Z$3')
        // AB12 = column #28, 0-based index 27, row #12, 0-based index 11
        assertEquals([table: null, sheet: null, column: 'AB', columnIndex: 27, row: 12, rowIndex: 11, coord: 'AB12'], Coord.'AB12')
        assertEquals([table: null, sheet: null, column: 'AB', columnIndex: 27, row: 12, rowIndex: 11, coord: '$AB$12'], Coord.'$AB$12')
    }

    void testCoordinateWithSheet() {
        // Sheet1!AA54 = Sheet1, column #27, 0-based index 26, row #54, 0-based index 53
        assertEquals([table: 'Sheet1', sheet: 'Sheet1', column: 'AA', columnIndex: 26, row: 54, rowIndex: 53, coord: 'Sheet1!AA54'], Coord.'Sheet1!AA54')
        assertEquals([table: 'Sheet1', sheet: 'Sheet1', column: 'AA', columnIndex: 26, row: 54, rowIndex: 53, coord: 'Sheet1!AA$54'], Coord.'Sheet1!AA$54')
    }

}
