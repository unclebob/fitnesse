/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.specify;

public class StartDoSpecification extends fit.Fixture {
	public static SystemUnderTest SUT;

	public StartDoSpecification() {
		SUT = new SystemUnderTest();
	}
}
