/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class RowFixtureUnderTest2 extends fit.RowFixture {

	public Object[] query() throws Exception {
		return new Object[0];
	}
	public Class getTargetClass() {
		return RowFixtureUnderTest.MockClass.class;
	}
}
