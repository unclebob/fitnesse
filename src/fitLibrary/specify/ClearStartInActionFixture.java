/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

import fit.ActionFixture;

/**
 * Clear the actor of ActionFixture to be sure that it's clear in a subsequent table.
 */
public class ClearStartInActionFixture extends fit.Fixture {
	public ClearStartInActionFixture() {
		ActionFixture.actor = null;
	}
}
