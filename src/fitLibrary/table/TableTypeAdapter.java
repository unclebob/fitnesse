/*
 * @author Rick Mugridge 12/02/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.table;

import fitLibrary.MetaTypeAdapter;
import fit.Parse;
import fit.exception.FitFailureException;
import fitLibrary.MetaTypeAdapter;

/**
 * 
 */
public class TableTypeAdapter extends MetaTypeAdapter {
	   public TableTypeAdapter(Class type) {
        this.type = type;
    }
    public static boolean applicableType(Class type) {
        return  fitLibrary.table.TableInterface.class.isAssignableFrom(type);
    }
    public Object parse(Parse parse) {
    	if (parse == null)
    		throw new FitFailureException("Missing table");
        Object[] args = new Object[]{parse};
        Class[] argTypes = new Class[]{ Parse.class };
        return callReflectively("parseTable",args,argTypes,null);
     }
    public String toString(Object object) {
	    if (object == null)
	    	return "null";
        return callReflectively("toTable",new Object[]{},new Class[]{},object).toString();
    }
    public boolean equals(Object expected, Object actual) {
        if (expected == null)
            return actual == null;
        return Table.equals(expected,actual);
    }
    // Is registered in LibraryTypeAdapter.on()
}
