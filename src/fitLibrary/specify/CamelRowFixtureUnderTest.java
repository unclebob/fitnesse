/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

/**
 *
 */
public class CamelRowFixtureUnderTest extends fit.RowFixture {

    public Object[] query() throws Exception {
         return new MockCollection[]{
    			new MockCollection(1,"one"),
				new MockCollection(1,"two"),
				new MockCollection(2,"two")};
    }
    public Class getTargetClass() {
        return MockCollection.class;
    }

}
