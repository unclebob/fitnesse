/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class SetFixtureUnderTest extends fitLibrary.SetFixture {
	public SetFixtureUnderTest() throws Exception {
		super(new CamelRowFixtureUnderTest().query());
	}
}
