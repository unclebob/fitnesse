/*
 * @author Rick Mugridge on Jan 4, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.tree;

import fitLibrary.MetaTypeAdapter;

/**
 * A TypeAdapter that handles Tree values instead of Strings
 */
public class TreeTypeAdapter extends MetaTypeAdapter {
    public TreeTypeAdapter(Class type) {
        this.type = type;
    }
    public static boolean applicableType(Class type) {
        return  fitLibrary.tree.TreeInterface.class.isAssignableFrom(type);
    }
    public Object parse(String s) {
        ListTree tree = ListTree.parse(s);
        Object[] args = new Object[]{tree};
        Class[] argTypes = new Class[]{ Tree.class };
        return callReflectively("parseTree",args,argTypes,null);
     }
    public String toString(Object object) {
	    if (object == null)
	    	return "null";
        return callReflectively("toTree",new Object[]{},new Class[]{},object).toString();
    }
    public boolean equals(Object a, Object b) {
        if (a == null)
            return b == null;
        return ListTree.equals(a,b);
    }
    // Is registered in LibraryTypeAdapter.on()
}
