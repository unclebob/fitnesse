/*
 * @author Rick Mugridge 12/02/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.specify;

import fitLibrary.DoFixture;
import fit.Parse;
import fit.exception.FitParseException;
import fitLibrary.table.Table;

public class DoTable extends DoFixture {
	public String firstCellStringValue(Table table) {
		return table.stringAt(0,0,0);
	}
	public Table firstCellValue(Table table) {
		return table.tableAt(0,0,0);
	}
	public Table aTable() throws FitParseException {
		return new Table(new Parse("<html><table><tr><td>one</td><td>two</td><td>three</td></tr></table></html>"));
	}
	public Table nullTable() {
		return null;
	}
}
