/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary.ff;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import fit.*;
import fit.exception.FitFailureException;
import fitLibrary.exception.IgnoredException;

/** 
 * A fixture for testing fixtures. Checks that the markings for each cell in a row are correct.
 * Checks that expected row insertions have occurred correctly and that no other insertions have occurred.
*/
public class FixtureFixture extends Fixture { 
	protected static final String REPORT = "report";
	protected final String INSERT_ROW = "I";
	protected static Counts embeddedCounts = new Counts();
	protected static Map embeddedSummary = new HashMap();
	protected Parse embeddedRow, embeddedTable;
	
	static {
		embeddedSummary.put("run date", new Date());
		embeddedSummary.put("run elapsed time", new Fixture().new RunTime());
			// RunTime should be a static inner class
	}
	public void doTable(Parse table) {
		try {
			super.doTable(table);
		} catch (FitFailureException ex) {
			failure(table.at(0, 0, 0), ex.getMessage());
		} catch (IgnoredException ex) {
		}
	}
	public void doRows(final Parse givenRows) {
		Parse rows = givenRows;
		embeddedRow = getFirstEmbeddedRow(rows.parts);
		embeddedTable = new Parse("table", "", embeddedRow, null);
		rows = rows.more;
		while (rows != null) {
			doCells(rows.parts);
			rows = rows.more;
		}
		doEmbeddedTable(embeddedTable);
		if (splicedInAddedRows(givenRows,embeddedTable.parts))
			checkMarkings(givenRows);
		else
			showActualRowsAtBottom(givenRows,embeddedTable.parts);
	}
	protected void showActualRowsAtBottom(Parse givenRows, Parse embeddedRows) {
		Parse rows = givenRows;
		while (rows.more != null)
			rows = rows.more;
		rows.more = new Parse("tr",null,new Parse("td","Actual Rows:",null,null),null);
		rows = rows.more;
		wrong(rows.parts);
		while (embeddedRows != null) {
			rows.more = new Parse("tr",null,new Parse("td","",null,embeddedRows.parts),null);
			rows = rows.more;
			embeddedRows = embeddedRows.more;
		}
	}
	protected boolean splicedInAddedRows(Parse outerRows, Parse embeddedRows) {
		while (outerRows != null && embeddedRows != null) {
			if (outerRows.parts.text().startsWith(INSERT_ROW)) {
				if (outerRows.parts.more != null && !validRowValues(outerRows.parts.more,embeddedRows.parts))
					return false;
				outerRows.parts.more = embeddedRows.parts;
				embeddedRows = embeddedRows.more;
			}
			else if (outerRows.parts.text().equals(REPORT))
				; // Do nothing; it will be picked up on the next pass
			else if (outerRows.parts.more == null)
				return false;
			else if (outerRows.parts.more == embeddedRows.parts) // They match, so OK
				embeddedRows = embeddedRows.more;
			else // Don't match, so wrong
				return false;
			outerRows = outerRows.more;
		}
		while (outerRows != null && outerRows.parts.text().equals(REPORT))
			outerRows = outerRows.more;
		if (outerRows != null || embeddedRows != null)
			return false;
		return true;
	}
	private boolean validRowValues(Parse expected, Parse actual) {
		while (expected != null && actual != null) {
			if (!expected.text().equals("")) {
				if (!expected.text().equals(actual.text()))
					return false;
			}
			expected = expected.more;
			actual = actual.more;
		}
		return expected == null && actual == null;
	}
	
	private Parse getFirstEmbeddedRow(Parse cells) {
		if (cells == null)
			throw new FitFailureException("Embedded fixture is missing");
		if (cells.text().equals("fixture") && cells.more != null)
			return new Parse("<tr>", "", cells.more, null);
		else {
			failure(cells, "Embedded fixture is missing");
			throw new IgnoredException();
		}
	}
	public void doCells(Parse cells) {
		if (cells.text().equals(REPORT) || cells.more == null || cells.text().startsWith(INSERT_ROW))
			return;
		Parse newRow = new Parse("<tr>", "", cells.more, null);
		embeddedRow.more = newRow;
		embeddedRow = newRow;
	}
	private void doEmbeddedTable(Parse tables) {
		Parse heading = tables.at(0, 0, 0);
		if (heading != null) {
			try {
				Fixture fixture = loadFixture(heading.text());
					//(Fixture) (Class.forName(heading.text()).newInstance());
				runFixture(tables, fixture);
//			} catch (ClassNotFoundException ex) {
//				failure(heading, ": Unknown Class");
			} catch (Throwable e) {
				exception(heading, e);
			}
		}
	}
	private void runFixture(Parse tables, Fixture fixture) {
		fixture.counts = embeddedCounts;
		fixture.summary = embeddedSummary;
		fixture.doTable(tables);
	}
	public void failure(Parse cell, String message) {
		wrong(cell);
		cell.addToBody(label(message));
	}
	private void checkMarkings(Parse rows) {
		checkRowMarkings(rows.more);
	}
	protected void checkRowMarkings(Parse rows) {
		Parse previousRow = null;
		while (rows != null) {
			Parse cells = rows.parts;
			if (cells.text().equals(INSERT_ROW))
				;
			else if (cells.text().equals(REPORT) && previousRow != null) {
				if (validReportValuesMarked(cells.more, previousRow.parts.more))
					right(cells);
				else
					wrong(cells);
			} else {
				String result = cellMarkings(cells.more);
				if (markingsEqual(unexclaim(cells.text()), result))
					right(cells);
				else
					wrong(cells, result);
			}
			previousRow = rows;
			rows = rows.more;
		}
	}
	private String unexclaim(String text) {
		if (text.startsWith(INSERT_ROW))
			return text.substring(1);
		return text;
	}
	private boolean validReportValuesMarked(Parse expected, Parse actual) {
		boolean result = true;
		while (expected != null && actual != null) {
			if (!expected.text().equals("")) {
				if (expected.text().equals(actual.text()))
					right(expected);
				else {
					wrong(expected,actual.text());
					result = false;
				}
			}
			expected = expected.more;
			actual = actual.more;
		}
		return result;
	}
	protected String cellMarkings(Parse cells) {
		String result = "";
		while (cells != null) {
		    result += fitLibrary.ff.Coloring.getColor(cells);
			cells = cells.more;
		}
		return result;
	}
	private boolean markingsEqual(String expected, String actual) {
		if (expected.equals(actual))
			return true;
		String trimmedActual = actual;
		while (trimmedActual.endsWith("-")) {
			trimmedActual =
				trimmedActual.substring(0, trimmedActual.length() - 1);
			if (expected.equals(trimmedActual))
				return true;
		}
		return false;
	}
}
