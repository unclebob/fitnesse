/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import fitLibrary.FitLibraryFixture;
import fit.*;

/**
 * An abstract superclass of DoFixture which defines the top-level
 * interpretation of tables.
 */
public abstract class FlowFixture extends FitLibraryFixture {
	private boolean stopOnError = false;

	protected void interpretTables(Parse tables) {
		if (tables.parts.more != null) {
			// Interpret any actions in the rest of the table:
			Parse restOfTable = new Parse("table","",tables.parts,null);
            doTable(restOfTable);
		}
		listener.tableFinished(tables);  
		tables = tables.more;
	    while (tables != null && !(stopOnError && problem(counts))) {
	        interpretTable(tables);
	        listener.tableFinished(tables);
	        tables = tables.more;
	    }
	}
    private void interpretTable(Parse table) {
		try {
			if (wasFixtureByName(table)) {
				return;
			}
			Parse cells = table.parts.parts;
			Object result = interpretCells(cells);
			if (result instanceof Fixture)
				interpretTableWithFixture(table, (Fixture)result);
			else // do the rest of the table with this fixture
				doTable(table);
		} catch (Exception e) {
			exception(table.at(0, 0, 0),e);
		}
	}
	protected void interpretTableWithFixture(Parse table, Fixture fixture) {
        fixture.counts = counts;
        fixture.summary = summary;
        fixture.doTable(table);
    }
    private boolean wasFixtureByName(Parse table) {
		Fixture fixture;
		try {
			fixture = getLinkedFixtureWithArgs(table);
		} catch (Throwable e) {
			return false;
		}
		fixture.doTable(table);
		return true;
	}
	public Fixture showSummary() {
		return new SummaryFixture();
	}
	public boolean isStopOnError() {
		return stopOnError;
	}
	/**
	 * if (stopOnError) then we don't continue intepreting a table
	 * if there's been a problem
	 */
	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}
	private boolean problem(Counts official) {
		return official.wrong + official.exceptions > 0;
	}

	protected abstract Object interpretCells(Parse table);
}
