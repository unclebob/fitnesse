/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class ArrayFixtureUnderTest2 extends fitLibrary.ArrayFixture {
	public ArrayFixtureUnderTest2() throws Exception {
		super(new Object[]{
    			new MockCollection(1,"one"),
				new MockCollection(1,"two"),
				new MockCollection(2,"two"),
				new Some()});
	}
	public static class Some {
		public String getSome() {
			return "one";
		}
	}
}
