/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.ff;

import fit.Parse;
import junit.framework.TestCase;

/**
 * @author rick
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FlowFixtureFixtureTest extends TestCase {
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(FlowFixtureFixtureTest.class);
	}
	public void testMakeEmbeddedTable() {
		Parse cells = td("",td("a",null));
		Parse rows = tr(cells,null);
		Parse table = new Parse("table","",rows,null);
		Parse result = new FlowFixtureFixture().makeEmbeddedRows(table.parts);
		assertEquals("a",result.parts.parts.text());
		assertNull(result.more);
		assertNull(result.parts.more);
		assertNull(result.parts.parts.more);
	}
	public void testMakeEmbeddedTable2() {
		Parse cells = td("",td("a",td("b",null)));
		Parse rows = tr(cells,null);
		Parse table = new Parse("table","",rows,null);
		Parse result = new FlowFixtureFixture().makeEmbeddedRows(table.parts);
		assertEquals("a",result.parts.parts.text());
		assertEquals("b",result.parts.parts.more.text());
		assertNull(result.more);
		assertNull(result.parts.more);
		assertNull(result.parts.parts.more.more);
	}
	public void testMakeEmbeddedTables() {
		Parse cells = td("",td("a",td("b",null)));
		Parse rows = tr(cells,tr(cells,null));
		Parse tables = new Parse("table","",rows,null);
		Parse resultingTable = new FlowFixtureFixture().makeEmbeddedTables(tables);
		assertEquals("a",resultingTable.parts.parts.text());
		assertEquals("b",resultingTable.parts.parts.more.text());
		assertNull(resultingTable.more);
		assertNull(resultingTable.parts.more);
		assertNull(resultingTable.parts.parts.more.more);
		
		assertEquals(tables.parts.parts.more,resultingTable.parts.parts);
	}
	private Parse tr(Parse cells,Parse more) {
		return new Parse("tr","",cells,more);
	}
	private Parse td(String s,Parse more) {
		return new Parse("td",s,null,more);
	}

}
