/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.ff;

import fit.ColumnFixture;
import fit.Fixture;
import fit.Parse;
import fit.exception.FitParseException;
import fit.exception.FitFailureException;
import junit.framework.TestCase;

/**
 *
 */
public class ColoringTest extends TestCase {
    Fixture fixture = new ColumnFixture();
    Parse cell;

    public void setUp() {
        cell = new Parse("td","blank",null,null);
    }
    public void testNone() throws FitParseException {
        assertEquals("-",code());
    }
    public void testRight() throws FitParseException {
        fixture.right(cell);
        assertEquals("r",code());
    }
    public void testWrong() throws FitParseException {
        fixture.wrong(cell);
        assertEquals("w",code());
    }
    public void testWrongExpected() throws FitParseException {
        fixture.wrong(cell,"expected");
        assertEquals("w",code());
    }
    public void testIgnore() throws FitParseException {
        fixture.ignore(cell);
        assertEquals("i",code());
    }
    public void testError() throws FitParseException {
        fixture.exception(cell, new FitFailureException(""));
        assertEquals("e",code());
    }
    public void testException() throws FitParseException {
        fixture.exception(cell, new RuntimeException(""));
        assertEquals("e",code());
    }
    private String code() {
        return Coloring.getColor(cell);
    }
}
