/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.specify;

import fitLibrary.SetUpFixture;

public class SetUpFixtureUnderTest2 extends SetUpFixture {
    private boolean setup = false;
    protected void setUp() {
        setup = true; 
    }
	public void aPercent(int a, int b) {
		if (!setup)
		    throw new RuntimeException();
	}
    protected void tearDown() {
        throw new RuntimeException("teardown");
    }
}
