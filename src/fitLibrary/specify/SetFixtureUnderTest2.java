/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class SetFixtureUnderTest2 extends fitLibrary.SetFixture {
	public SetFixtureUnderTest2() throws Exception {
		super(new Object[]{
    			new MockCollection(1,"one"),
				new MockCollection(1,"two"),
				new MockCollection(1,"two"),
				new Some()});
	}
	public static class Some {
		public String getSome() {
			return "one";
		}
	}
}
