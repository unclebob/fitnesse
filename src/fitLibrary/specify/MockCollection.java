/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class MockCollection {
	public int plus = 0;
	public String ampersand;
	
	public MockCollection(int plus, String ampersand) {
		this.plus = plus;
		this.ampersand = ampersand;
	}
	public int getProp() {
	    return plus;
	}
}
