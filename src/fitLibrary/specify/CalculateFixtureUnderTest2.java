/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;


/**
 *
 */
public class CalculateFixtureUnderTest2 extends fitLibrary.CalculateFixture {
    public CalculateFixtureUnderTest2() {
        setRepeatString("\"");
        setExceptionString("exception");
    }
    public int plusAB(int a, int b) {
		return a + b;
	}
	public String exceptionMethod() {
	    throw new RuntimeException("Expected exception");
	}
}
