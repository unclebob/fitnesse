/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fitLibrary.exception.ExtraCellsFailureException;
import fitLibrary.exception.MissingCellsFailureException;
import fitLibrary.ArrayFixture;
import fit.*;

/**
 * A more flexible alternative to RowFixture:
 * o The collection may be provided as an array, Collection or Iterator
 * o The column names may refer to properties of an element Object.
 * o The elements don't have to be of related types. Where a column doesn't apply
 *   to a particular element, the expected value must be blank.
 * 
 * For large sets, this is a more expensive algorithm than used in RowFixture.
 */
public class SetFixture extends ArrayFixture {
	public SetFixture() {
		super();
	}
	public SetFixture(Iterator actuals) {
		super(actuals);
	}
	public SetFixture(Collection actuals) {
		super(actuals);
	}
	public SetFixture(Object[] actuals) {
		super(actuals);
	}
    protected void doRow(Parse row, List actuals) throws Exception {
    	if (actuals.isEmpty()) {
			missing(row);
			return;   		
    	}
    	int rowLength = row.parts.size();
		TypeAdapter[] columnBindings = (TypeAdapter[])actuals.get(0);
		if (rowLength < columnBindings.length)
			throw new MissingCellsFailureException();
		if (rowLength > columnBindings.length)
			throw new ExtraCellsFailureException();
		List matchingActuals = actuals;
		for (int column = 0; column < columnBindings.length; column++) {
			matchingActuals = matchOnColumn(row,matchingActuals,column);
			if (matchingActuals.isEmpty()) {
				missing(row);
				return;
			}
			if (matchingActuals.size() == 1) {
				TypeAdapter[] theOne = (TypeAdapter[])matchingActuals.get(0);
				matchRow(row.parts,theOne);
				actuals.remove(theOne);
				return;
			}
		}
		// There may be > 1 actuals that matched, so match on the first one.
		if (!matchingActuals.isEmpty()) {
			TypeAdapter[] theOne = (TypeAdapter[])matchingActuals.get(0);
			matchRow(row.parts,theOne);
			actuals.remove(theOne);
		}
	}
    /* Returns the subset of actuals that match on the given column of the row */
	private List matchOnColumn(Parse row, List actuals, int column) {
		List results = new ArrayList();
		for (Iterator it = actuals.iterator(); it.hasNext(); ) {
			TypeAdapter[] columnBindings = (TypeAdapter[])it.next();
			TypeAdapter adapter = columnBindings[column];
			Parse cell = row.parts.at(column);
			if (adapter == null) { // Doesn't apply
				if (cell.text().equals(""))
					results.add(columnBindings);
			}
			else {
				try {
					if (matches(adapter, cell))
						results.add(columnBindings);
				} catch (Exception e) {
				}
			}
		}
		return results;
	}
}
