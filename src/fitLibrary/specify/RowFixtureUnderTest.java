/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class RowFixtureUnderTest extends fit.RowFixture { //COPY:ALL
	//COPY:ALL
	public Object[] query() throws Exception { //COPY:ALL
		return new MockClass[]{ //COPY:ALL
			new MockClass(1,"one"), //COPY:ALL
			new MockClass(1,"two"), //COPY:ALL
			new MockClass(2,"two")}; //COPY:ALL
	} //COPY:ALL
	public Class getTargetClass() { //COPY:ALL
		return MockClass.class; //COPY:ALL
	} //COPY:ALL
	public class MockClass { //COPY:ALL
		public int a = 0; //COPY:ALL
		public String s; //COPY:ALL
		public double camelField = 1.5;
		public MockClass(int a, String s) { //COPY:ALL
			this.a = a; //COPY:ALL
			this.s = s; //COPY:ALL
		} //COPY:ALL
	} //COPY:ALL
} //COPY:ALL
