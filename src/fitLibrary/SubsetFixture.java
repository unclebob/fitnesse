/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fit.Parse;
import fitLibrary.SetFixture;

/**
 * Like SetFixture, except that it ignores any surplus, unmatched elements in the
 * actual collection. That is, the table specifies an expected subset.
 */
public class SubsetFixture extends SetFixture {
	public SubsetFixture() {
		super();
	}
	public SubsetFixture(Iterator actuals) {
		super(actuals);
	}
	public SubsetFixture(Collection actuals) {
		super(actuals);
	}
	public SubsetFixture(Object[] actuals) {
		super(actuals);
	}
	protected void showSurplus(List bindings, Parse last) {
	}
}
