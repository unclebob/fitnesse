/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;
import fit.RowFixture;

public class EmptyRowFixture extends RowFixture {

	public Object[] query() throws Exception {
		return new Object[] {};
	}
	public Class getTargetClass() {
		return java.awt.Point.class;
	}
}
