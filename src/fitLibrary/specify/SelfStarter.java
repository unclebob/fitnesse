/*
 * @author Rick Mugridge on Dec 28, 2004
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

/**
 *
 */
public class SelfStarter extends SelfStartingActionFixture {
    private String s;

    public void enterString(String s) {
        this.s = s;
    }
    public String s() {
        return s;
    }
}
