/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.specify;

import fitLibrary.SetUpFixture;
import fitLibrary.SetUpFixture;

public class SetUpFixtureUnderTest extends SetUpFixture {
	public void aB(int a, int b) {
		if (a < 0)
		    throw new RuntimeException();
	}
}
