/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import fit.exception.FitFailureException;
import fit.*;
import fitLibrary.ExtendedCamelCase;

/**
 * An abstract superclass of all the flow-style fixtures.
 * Allows a single object to manage parsing of other types, with the
 * parseValue() method.
 */
public abstract class FitLibraryFixture extends Fixture {
//	private static Map PARSE_DELEGATES = new HashMap();
	
	/** Registers a delegate, a class that will
	 * handle parsing of other types of values.
	 */
	protected void registerParseDelegate(Class type, Class parseDelegate) {
	    LibraryTypeAdapter.registerParseDelegate(type,parseDelegate);
	}
	/** Registers a delegate object that will
	 * handle parsing of other types of values.
	 */
	protected void registerParseDelegate(Class type, Object parseDelegate) {
	    LibraryTypeAdapter.registerParseDelegate(type,parseDelegate);
	}
	/** Overrides so as to handle any parsing through the delegate.
	 *  It can be overridden in subclasses and used in the original way (in
	 *  which case, as usual, the method should call super.parse().
	 */
	public Object parse(String s, Class type) throws Exception {
        if (type.equals(String.class))
            return s; // Avoid FitNesse String magic
		return super.parse(s, type);
	}
    public static String extendedCamel(String name) {
        return ExtendedCamelCase.camel(name);
    }
    public void exception(Parse cell, String message) {
        exception(cell, new FitFailureException(message));
    }
    public void rightHtml(Parse cell, String actual) {
        right(cell);
        cell.body = actual;
    }
    public void wrongHtml(Parse cell, String actual) {
          wrong(cell);
          cell.addToBody(label("expected") + "<hr>" + actual
              + label("actual"));
    }
    // Simple String replace that can handle special chars like "\"
	protected static String replaceString(String text, String pattern, String replacement) {
		int pos = 0;
		while (true) {
			pos = text.indexOf(pattern,pos);
			if (pos < 0)
				break;
			text = text.substring(0,pos)+replacement+
						text.substring(pos+pattern.length());
			pos += replacement.length();
		}
		return text;
	}
    // NOW WE ALLOW FOR DIFFERENCES BETWEEN FIT IN AND OUT OF FITNESSE
    
    protected final static String FITNESSE_FILES_LOCATION = 
        "FitNesseRoot"+File.separator+"files";
    
    public static File getRelativeFile(String name) {
        if (IN_FITNESSE) {
            return new File(getHomeDirectoryAllowingForFitNesse(),name);
        }
        else
            return new File(name);
    }
	protected static File getHomeDirectoryAllowingForFitNesse() {
	    if (IN_FITNESSE) {
	        File fitNesse = new File(FITNESSE_FILES_LOCATION);
	        if (!fitNesse.exists() || !fitNesse.isDirectory())
	            throw new RuntimeException("The FitNesse directories have changed: "+
	                    fitNesse.getAbsolutePath());
	        return fitNesse;
	    }
	    else
	        return new File(".");
	    
	}
    protected static String htmlLink(File file) {
        return "<a href=\""+url(file)+"\">"+file.getName()+"</a>";
    }
    public static String htmlImageLink(File file) {
        return "<img src=\""+url(file)+"\">";
    }
    protected static String url(File file) {
        String absolutePath = file.getAbsolutePath();
        String link = "";
        if (IN_FITNESSE) {
            int index = absolutePath.indexOf(FITNESSE_FILES_LOCATION);
            if (index < 0)
                throw new RuntimeException("Unable to localise FitNesse files with path "+
                        absolutePath);
            String within = absolutePath.substring(index+FITNESSE_FILES_LOCATION.length());
            within = replaceString(within,"\\","/");
            link = "http:/files"+within;
        }
        else
            link = "file:///"+absolutePath;
        return link;
    }
    public static final boolean IN_FITNESSE = true; // Here to simplify source comparison
/*    
    public void doTable(Parse table) { // Not needed with FitNesse as it already has FitFailureException
    	try {
    		super.doTable(table);
    	}
    	catch (Throwable ex) {
    		exception(table.at(0, 0, 0), ex);
    	}
    }
    public void exception (Parse cell, Throwable exception) { // Not needed with FitNesse
        while (exception.getClass().equals(InvocationTargetException.class)) {
            exception = ((InvocationTargetException)exception).getTargetException();
        }
        if (exception instanceof FitFailureException)
            cell.addToBody("<hr/>" + label(exception.getMessage()));
        else {
            final StringWriter buf = new StringWriter();
            exception.printStackTrace(new PrintWriter(buf));
            cell.addToBody("<hr><pre><font size=-2>" + (buf.toString()) + "</font></pre>");
        }
        cell.addToTag(" bgcolor=\"" + yellow + "\"");
        counts.exceptions++;
    }
*/
}
