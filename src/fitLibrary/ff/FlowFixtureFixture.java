/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary.ff;
import fit.*;

/** 
 * A fixture for testing fixtures in flow style.
 * It is in flow style itself because it overrides the method interpretTables(),
 * which is called in the revised fit.Fixture.
*/
public class FlowFixtureFixture extends FixtureFixture { 
	protected void interpretTables(Parse tables) {
		Parse embeddedTables = makeEmbeddedTables(tables);
		new Fixture().doTables(embeddedTables);
		checkMarkingsOfTables(tables,embeddedTables);
		signalTables(tables);
	}
	private void checkMarkingsOfTables(Parse tables, Parse embeddedTables) {
		checkRowMarkings(tables.parts.more);
		tables = tables.more;
		embeddedTables = embeddedTables.more;
		while (tables != null) {
			checkMarkingOfRows(tables.parts, embeddedTables.parts);
			tables = tables.more;
			embeddedTables = embeddedTables.more;
		}
	}
	private void checkMarkingOfRows(Parse outerRows, Parse innerRows) {
		if (splicedInAddedRows(outerRows,innerRows))
			checkRowMarkings(outerRows);
		else
			showActualRowsAtBottom(outerRows,innerRows);
	}
	protected Parse makeEmbeddedTables(Parse tables) {
		Parse allEmbeddedTables = new Parse("table", "", null, null);
		Parse embeddedTables = allEmbeddedTables;
		Parse rowsAfterFirst = tables.parts.more;
		if (rowsAfterFirst != null)
			// Handle rest of first table as a separate table:
			embeddedTables = addTable(embeddedTables, rowsAfterFirst);
		tables = tables.more;
		while (tables != null) {
			embeddedTables = addTable(embeddedTables, tables.parts);
			tables = tables.more;
		}
		return allEmbeddedTables.more;
	}
	private Parse addTable(Parse embeddedTables, Parse rows) {
		embeddedTables.more = makeEmbeddedRows(rows);
		return embeddedTables.more;
	}
	protected Parse makeEmbeddedRows(Parse rows) {
		Parse allEmbeddedRows = new Parse("tr",null,null,null);
		Parse embeddedRow = allEmbeddedRows;
		while (rows != null) {
			Parse cells = rows.parts;
			if (!cells.text().equals(REPORT) && cells.more != null && !cells.text().startsWith(INSERT_ROW)) {
				embeddedRow.more = new Parse("tr",null,cells.more,null); //drop first column
				embeddedRow = embeddedRow.more;
			}
			rows = rows.more;
		}
		return new Parse("table", "", allEmbeddedRows.more, null);
	}
	private void signalTables(Parse tables) {
		while (tables != null) {
			listener.tableFinished(tables);
			tables = tables.more;
		}
	}
}
