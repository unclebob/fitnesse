/*
 * @author Rick Mugridge on Jan 10, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import fitLibrary.graphic.GraphicTypeAdapter;
import fitLibrary.table.TableTypeAdapter;
import fitLibrary.tree.TreeTypeAdapter;
import fitLibrary.FitLibraryFixture;
import fit.*;

/**
 * Extends the core TypeAdapter to allow for:
 * o Registered delegates, which will handle type adaptation
 * o Types where the class has a static parse() method.
 */
public class LibraryTypeAdapter {
	private static Map PARSE_DELEGATES = new HashMap();
	
    /**
     * For return types, we can use String compare by default
     */
    public static TypeAdapter onResult(FitLibraryFixture fixture, Class returnType) {
        TypeAdapter result = on(fixture,returnType);
        if (result.getClass() == TypeAdapter.class) // ie, nothing works
            return new ResultTypeAdapterByStringCompare(returnType);
        return result;
    }
    public static TypeAdapter on(Fixture target, Class type) {
        if (TreeTypeAdapter.applicableType(type)) // SMELL.....
            return new TreeTypeAdapter(type);
        else if (TableTypeAdapter.applicableType(type))
            return new TableTypeAdapter(type);
        else if (GraphicTypeAdapter.applicableType(type))
            return new GraphicTypeAdapter(type);
        TypeAdapter a = adapterFor(type);
        a.init(target, type);
        return a;
    }
    public static TypeAdapter on(FitLibraryFixture fixture, Field field) {
        TypeAdapter a = on(fixture, field.getType());
        a.target = fixture;
        a.field = field;
        return a;
    }
    public static TypeAdapter on(FitLibraryFixture fixture, Method method) {
        TypeAdapter a = onResult(fixture, method.getReturnType());
        a.target = fixture;
        a.method = method;
        return a;
    }
    private static TypeAdapter adapterFor(Class type) throws UnsupportedOperationException {
        Object delegate = PARSE_DELEGATES.get(type);
		if (delegate != null)
		    return (TypeAdapter)delegate;
        TypeAdapter adapter = TypeAdapter.adapterFor(type);
        if (adapter.getClass() == TypeAdapter.class) {
            try { // No match, so try for a static parse() method
                DelegateClassAdapter classAdapter = new DelegateClassAdapter(type);
                PARSE_DELEGATES.put(type,classAdapter);
                return classAdapter;
            } catch (Exception e) {
            }
        }
        return adapter;
    }
    /** Take account of values that may be in HTML or in Parse */
	public static Object parse(Parse cell, TypeAdapter typeAdapter) throws Exception {
		if (typeAdapter instanceof TableTypeAdapter)
			return ((TableTypeAdapter)typeAdapter).parse(cell.parts);
	    String text = cell.text();
	    if (getTextFromBody(typeAdapter))
            text = cell.body;
        return typeAdapter.parse(text);
    }
	/** When we need to access the underlying HTML */
	private static boolean getTextFromBody(TypeAdapter typeAdapter) {
		return typeAdapter instanceof MetaTypeAdapter;
	}
	/** Registers a delegate, a class that will handle parsing of other types of values.
	 */
	public static void registerParseDelegate(Class type, Class parseDelegate) {
	    try {    
	        PARSE_DELEGATES.put(type,new DelegateClassAdapter(parseDelegate));
	    } catch (Exception ex) {
	        throw new RuntimeException("Parse delegate class "+
	                parseDelegate.getName()+
	                " does not have a suitable static parse() method.");
	    }
	}
	/** Registers a delegate object that will handle parsing of other types of values.
	 */
	public static void registerParseDelegate(Class type, Object parseDelegate) {
	    try {
	        PARSE_DELEGATES.put(type,new DelegateObjectAdapter(parseDelegate));
	    } catch (Exception ex) {
	        throw new RuntimeException("Parse delegate object of class "+
	                parseDelegate.getClass().getName()+
	                " does not have a suitable parse() method.");
	    }
	}
	static class DelegateClassAdapter extends TypeAdapter {
	    private Method method;
	    
	    public DelegateClassAdapter(Class parseDelegate) throws SecurityException, NoSuchMethodException {
	        this.method = parseDelegate.getMethod("parse",new Class[] { String.class });
	        int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers) ||
	        	!Modifier.isPublic(modifiers) ||
	        	method.getReturnType() == Void.class)
                	throw new NoSuchMethodException();
	    }
		public Object parse(String s) throws Exception {
		    return method.invoke(null, new Object[] { s });
		}	    
	}
	static class DelegateObjectAdapter extends TypeAdapter {
	    private Object delegate;
	    private Method method;
	    
	    public DelegateObjectAdapter(Object delegate) throws SecurityException, NoSuchMethodException {
	        this.delegate = delegate;
	        this. method = delegate.getClass().getMethod("parse",
	                new Class[] { String.class });
	    }
		public Object parse(String s) throws Exception {
		    return method.invoke(delegate, new Object[] { s });
		}	    
	}
	// A version of the hasParseMethod() method in fit.Fixture in FitNesse20041105
	// Used in MethodTarget...
	public static boolean hasStaticParseMethod(Class type) {
	    try {
	        Method method = type.getMethod("parse",new Class[]{ String.class });
	        int modifiers = method.getModifiers();
            return Modifier.isStatic(modifiers) &&
	        	   Modifier.isPublic(modifiers) &&
	        	   method.getReturnType() != Void.class;
	    } catch (NoSuchMethodException e) {
	        return false;
	    }
	}
}
