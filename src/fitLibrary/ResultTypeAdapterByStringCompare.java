/*
 * @author Rick Mugridge on Jan 13, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary;

import fit.TypeAdapter;

/**
 * This adapter is used when there is none available. As it's just for
 * method results, a String compare will have to do.
 */
public class ResultTypeAdapterByStringCompare extends TypeAdapter {
    private Class returnType;

    public ResultTypeAdapterByStringCompare(Class returnType) {
        this.returnType = returnType;
    }
    public Object parse(String s) throws Exception {
        return s; // Just stay as a String -- it may be sufficient for a check
    }
    public String toString(Object actual) {
    	if (actual == null)
    		return "null";
        return actual.toString();
    }
    public boolean equals(Object a, Object b) { // a will be a String
        if (a == null)
            return b == null;
        if (b == null)
        	return false;
        return a.equals(b.toString());
    }
}
