/*
 * @author Rick Mugridge on Dec 28, 2004
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

import fit.Parse;

/**
 *
 */
public class SelfStartingActionFixture extends fit.ActionFixture {
    public void doTable(Parse table) {
        actor = this;
        super.doTable(table);
    }
}
