/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.ff;

import fit.Fixture;
import fit.Parse;
import fitLibrary.ParseUtility;
import junit.framework.TestCase;
import fitLibrary.ParseUtility;

/**
 *
 */
public class FixtureFixtureTest extends TestCase {
    public void test1() throws Exception {
        Parse table = new Parse("<table><tr><td>fitLibrary.ff.FixtureUnderTest</td>"+
                "<td>r</td>"+
                "</tr></table>\n");
        new Fixture().doTables(table);
        ParseUtility.printParse(table,"test");
    }
}
