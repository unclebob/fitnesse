/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import fit.RowFixture;

/**
 * Makes it easier to use a RowFixture with flow tables, as there is no
 * need to subclass.
 */
public class ParamRowFixture extends RowFixture {
	private Object[] objects;
	private Class targetClass;

	/** There needs to be at least one element in the collection for this to be used. */
	public ParamRowFixture(Object[] objects) {
		this(objects,objects[0].getClass());
	}
	public ParamRowFixture(Object[] objects, Class targetClass) {
		this.objects = objects;
		this.targetClass = targetClass;
	}
	public Object[] query() throws Exception {
		return objects;
	}
	public Class getTargetClass() {
		return targetClass;
	}
}
