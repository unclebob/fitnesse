/*
 * @author Rick Mugridge on Jan 4, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.graphic;

import java.io.File;

import fitLibrary.FitLibraryFixture;
import fitLibrary.MetaTypeAdapter;
import fitLibrary.FitLibraryFixture;

/**
 * A TypeAdapter that handles Graphic images
 */
public class GraphicTypeAdapter extends MetaTypeAdapter {
    public GraphicTypeAdapter(Class type) {
        this.type = type;
    }
    public static boolean applicableType(Class type) {
        return  GraphicInterface.class.isAssignableFrom(type);
    }
    public Object parse(String imageLink) {
        File file = new File(getImageFileName(imageLink));
        Object[] args = new Object[]{file};
        Class[] argTypes = new Class[]{ File.class };
        return callReflectively("parseGraphic",args,argTypes,null);
     }
    public String toString(Object object) {
	    if (object == null)
	    	return "null";
        return toImageLink((File)
                callReflectively("toGraphic",new Object[]{},new Class[]{},object));
    }
    public boolean equals(Object a, Object b) {
        if (a == null)
            return b == null;
        return a.equals(b);
    }
    public static String toImageLink(File imageFile) {
        return FitLibraryFixture.htmlImageLink(imageFile);
    }
	public static String getImageFileName(String html) {
		String match = "src=\"";
		int srcPos = html.indexOf(match);
		if (srcPos < 0)
			throw new RuntimeException("Not a valid graphic link: '"+html+"'");
		int start = srcPos+match.length();
		int end = html.indexOf("\"",start);
		return html.substring(start,end);
	}
    // Is registered in LibraryTypeAdapter.on()
}
