/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

import fit.Parse;

/**
 * Clear the counts for the inner fixtures to test fit.Summary.
 */
public class ResetCounts extends fit.Fixture {
	public void doTable(Parse table) {
		counts.right = 0;
		counts.wrong = 0;
		counts.exceptions = 0;
		counts.ignores = 0;
	}

}
