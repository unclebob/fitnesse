/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import fitLibrary.*;
import fit.Parse;

/**
 * Exactly the same as DoFixture, except that actions don't have keywords
 * in every second cell.
 */
public class SequenceFixture extends DoFixture {
	public SequenceFixture() {
	}
	public SequenceFixture(Object sut) {
		super(sut);
	}
	protected MethodTarget findMethodByActionName(Parse cells, int args) throws Exception {
		String actionName = cells.text();
		return findMethod(ExtendedCamelCase.camel(actionName), args);
	}
}
