/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.ff;
import fit.Parse;

/**
 * Extension of fat.Tests.Color.
 */
public class Coloring  {
    private static final String[] codes = {
            "pass", "r",
            "fail", "w",
            "ignore", "i",
            "fit_grey", "i",
            "error", "e",
            "fit_stacktrace", "e"};

	public static String getColor(Parse cell) {
	    if (cell == null)
	        return null;
	    String tag = cell.tag.toLowerCase();
	    for (int i = 0; i < codes.length; i += 2)
	        if (tag.indexOf(" class=\""+codes[i]+"\">") >= 0)
	            return codes[i+1];
	    return "-";
	}
}
