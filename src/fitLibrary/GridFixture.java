/*
 * @author Rick Mugridge 2/02/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary;

import fitLibrary.FitLibraryFixture;

import java.text.ParseException;

import fit.*;

/**
 * Checks the values in the table against the values in the 2D array
 */
public class GridFixture extends FitLibraryFixture {
	private Object[][] grid;
	private TypeAdapter typeAdapter;

	public GridFixture(Object[][] grid) {
		setGrid(grid);
	}
	protected GridFixture() {
	}
	public void setGrid(Object[][] grid) {
		this.grid = grid;
		typeAdapter = LibraryTypeAdapter.on(this,grid.getClass().getComponentType().getComponentType());
	}
	public void doTable(Parse table) {
		if (grid.length == 0 && table.parts.more == null)
			right(table.parts);
		else if (!rowsMatch(grid, table.parts))
			addActualRows(table.parts,grid);
	}
	private boolean rowsMatch(Object[][] actual, Parse lastRow) {
		boolean matched = true;
		for (int i = 0; i < actual.length; i++) {
			if (lastRow.more == null) {
				matched = false;
				break;
			}
			lastRow = lastRow.more;
			if (!cellsMatch(actual[i],lastRow.parts))
				matched = false;
		}
		return markWrong(lastRow.more, matched);
	}
	private boolean cellsMatch(Object[] actual, Parse allCells) {
		Parse cells = allCells;
		boolean matched = true;
		for (int i = 0; i < actual.length; i++) {
			if (!cellMatches(actual[i], cells))
				matched = false;
			if (cells.more == null && i < actual.length-1) {
				matched = false;
				break;
			}
			cells = cells.more;
		}
		return markWrong(cells, matched);
	}
	private boolean cellMatches(Object actual, Parse cell) {
		if (cell.body == null)
			return false;
		boolean matches = false;
		try {
			matches = typeAdapter.equals(
					LibraryTypeAdapter.parse(cell,typeAdapter),
					actual);
		} catch (Exception e) {
			// Doesn't match
		}
		if (matches)
			right(cell);
		else 
			wrong(cell);
		return matches;
	}
	/** Add extra cells to expected, if necessary.
	 */
	private boolean markWrong(Parse cells, boolean matched) {
		while (cells != null) {
			matched = false;
			wrong(cells);
			cells = cells.more;
		}
		return matched;
	}
	private void addActualRows(Parse rows, Object[][] actual) {
		int cols = 0;
		Parse lastRow = rows;
		while (rows != null) {
			lastRow = rows;
			cols = Math.max(cols,lastRow.size());
			rows = rows.more;	
		}
		for (int i = 0; i < actual.length; i++)
			cols = Math.max(cols,actual[i].length);
		lastRow.more = new Parse("tr",null,new Parse("td colspan="+cols,"<i>Actuals:</i>",null,null),null);
		lastRow = lastRow.more;
		for (int i = 0; i < actual.length; i++) {
			try {
				lastRow.more = makeRowWithTr(actual[i]);
				lastRow = lastRow.more;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	private Parse makeRowWithTr(Object[] actuals) throws ParseException {
		return new Parse("tr", null, makeCellsWithTd(actuals), null);
	}
	private Parse makeCellsWithTd(Object[] actuals) throws ParseException {
		if (actuals.length == 0)
			throw new RuntimeException("Actuals row empty");
		Parse rows = makeCell(actuals[0]);
		Parse row = rows;
		for (int i = 1; i < actuals.length; i++) {
			row.more = makeCell(actuals[i]);
			row = row.more;
		}
		return rows;
	}
	private Parse makeCell(Object object) {
		return new Parse("td", gray(typeAdapter.toString(object)), null, null);
	}
}
