/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fitLibrary.exception.AmbiguousNameFailureException;
import fit.exception.FitFailureException;
import fitLibrary.exception.IgnoredException;
import fitLibrary.*;
import fit.*;

/**
 * Manages calling a method on row cells, and possibly checking the result against a cell.
 * It constructs TypeAdapters to use for getting cell values, comparisons , etc.
 */
public class MethodTarget {
	private Object subject;
	private Method method;
	private FitLibraryFixture fixture;
	private TypeAdapter[] parameterAdapters;
	private TypeAdapter resultTypeAdapter = null;
	private Object[] args;
	private String repeatString = null;
    private String exceptionString = null;
	private boolean everySecond = false;

	public static MethodTarget findSpecificMethod(String name, int args, Object subject, FitLibraryFixture fixture) {
		Method[] methods = subject.getClass().getMethods();
		Method chosenMethod = null;
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (name.equals(method.getName()) &&
		            method.getParameterTypes().length == args)
				if (chosenMethod == null)
					chosenMethod = method;
				else
					throw new AmbiguousNameFailureException(name);
		}
		if (chosenMethod == null && args == 0)
		    chosenMethod = findProperty(name,methods);
		if (chosenMethod == null)
			return null;
		return new MethodTarget(subject,chosenMethod,fixture);
	}
	private static Method findProperty(String name, Method[] methods) {
	    Method chosenMethod = null;
	    String booleanPropertyName = ExtendedCamelCase.camel("is "+name);
	    String otherPropertyName = ExtendedCamelCase.camel("get "+name);
	    for (int i = 0; i < methods.length; i++) {
	        Method method = methods[i];
	        if (method.getParameterTypes().length == 0) {
	            boolean booleanResult = method.getReturnType() == boolean.class;
	            boolean aBoolean = booleanResult &&
	            	booleanPropertyName.equals(method.getName());
	            boolean aNonBoolean = !booleanResult &&
	            	otherPropertyName.equals(method.getName());
	            if (aBoolean || aNonBoolean)
	                if (chosenMethod == null)
	                    chosenMethod = method;
	                else
	                    throw new AmbiguousNameFailureException(name);
	        }
	    }
	    return chosenMethod;
	}
    public MethodTarget(Object subject, Method method, FitLibraryFixture fixture) {
		this.subject = subject;
		this.method = method;
		this.fixture = fixture;
		Class[] types = getParameterTypes();
		parameterAdapters = new TypeAdapter[types.length]; // Not wired in yet.
		args = new Object[types.length];
		for (int i = 0; i < types.length; i++)
		    parameterAdapters[i] = LibraryTypeAdapter.on(fixture,types[i]);
		Class returnType = getReturnType();
		if (returnType != void.class)
		    resultTypeAdapter = LibraryTypeAdapter.onResult(fixture,returnType);
    }
	public boolean isValid() {
		return method != null;
	}
	public Class getReturnType() {
		return method.getReturnType();
	}
	public Class[] getParameterTypes() {
		return method.getParameterTypes();
	}
	public Object invoke(Object[] args) throws Exception {
		return method.invoke(subject, args);
	}
	public Object invoke(Parse cells) throws Exception {
		try {
			if (everySecond)
				collectEverySecondCell(cells);
			else
				collectAllCells(cells);
		} catch (Exception e) {
			throw new IgnoredException(); // Unable to call
		}
		return invoke(args);
	}
	private void collectAllCells(Parse cells) throws Exception {
		for (int argNo = 0; argNo < args.length; argNo++) {
			collectCell(cells, argNo, cells.text());
			cells = cells.more;
		}
	}
    private void collectEverySecondCell(Parse cells) throws Exception {
		for (int argNo = 0; argNo < args.length; argNo++) {
			collectCell(cells, argNo, cells.text());
			cells = cells.more;
			if (cells != null)
				cells = cells.more;
		}
	}
	private void collectCell(Parse cells, int argNo, String text) throws Exception {
		try {
			if (!text.equals(repeatString))
				args[argNo] = LibraryTypeAdapter.parse(cells,parameterAdapters[argNo]);
		} catch (Exception e) {
		    fixture.exception(cells,e);
		    throw e;
		}
	}
	public void invokeAndCheck(Parse cells, Parse expectedCell) {
	    Object result = null;
 		boolean exceptionExpected = exceptionString != null &&
 				exceptionString.equals(expectedCell.text());
        try {
 			result = invoke(cells);
		    if (exceptionExpected) {
		        fixture.wrong(expectedCell);
		        return;
		    }
		} catch (IgnoredException ex) {
			return;
		} catch (FitFailureException e) {
		    return;
		} catch (Exception e) {
		    if (exceptionExpected)
		        fixture.right(expectedCell);
		    else
		        fixture.exception(expectedCell,e);
		    return;
		}
		try {
		    if (resultTypeAdapter == null)
		        throw new FitFailureException("No value provided");
		    String toString = resultTypeAdapter.toString(result);
		    if (resultTypeAdapter instanceof MetaTypeAdapter) {
		        Object expectedResult = LibraryTypeAdapter.parse(expectedCell,resultTypeAdapter);
                if (resultTypeAdapter.equals(expectedResult, result))
		            fixture.rightHtml(expectedCell,toString);
		        else
		            fixture.wrongHtml(expectedCell,toString);
		    }
		    else {
		        if (resultTypeAdapter.equals(resultTypeAdapter.parse(expectedCell.text()), result))
		            fixture.right(expectedCell);
		        else
		            fixture.wrong(expectedCell,toString);
		    }
		} catch (Exception e) {
		    fixture.exception(expectedCell,e);
		}
	}
	public void color(Parse cells, boolean right) throws Exception {
		while (cells != null) {
			if (right)
				fixture.right(cells);
			else
				fixture.wrong(cells);
			if (!everySecond)
				break;
			cells = cells.more;
			if (cells != null)
				cells = cells.more;
		}
	}
	/** Defines the Strings that signifies that the value in the row above is
	 *  to be used again. Eg, it could be set to "" or to '"".
	 */
	public void setRepeatAndExceptionString(String repeatString, String exceptionString) {
		this.repeatString = repeatString;
		this.exceptionString = exceptionString;
	}
	public void setEverySecond(boolean everySecond) {
		this.everySecond = everySecond;
	}
	private Object wrapObjectWithFixture(Object result) {
		if (isPrimitiveReturnType())
		    return result;
		else if (result instanceof String)
		    return result;
		else if (result instanceof Fixture)
		    return result;
		else if (isObjectArrayReturnType())
		    return new ArrayFixture((Object[])result);
		else if (returnAssignableTo(Set.class))
		    return new SetFixture(((Set)result).toArray());
		else if (returnAssignableTo(Collection.class))
		    return new ArrayFixture((Collection)result);
		else if (returnAssignableTo(Iterator.class))
		    return new ArrayFixture((Iterator)result);
		else if (returnAssignableTo(Map.class))
		    return new SetFixture(((Map)result).values().toArray());
		else if (LibraryTypeAdapter.hasStaticParseMethod(result.getClass()))
		    return result;
		else
		    return new DoFixture(result);
	}
	private boolean isPrimitiveReturnType() {
	    return getReturnType().isPrimitive();
	}
	private boolean isObjectArrayReturnType() {
        Class returnType = getReturnType();
        return returnType.isArray() &&
        	!returnType.getComponentType().isArray() &&
        	returnType.getComponentType().isAssignableFrom(Object.class);
    }
	private boolean returnAssignableTo(Class type) {
        return type.isAssignableFrom(getReturnType());
   }
    public Object invokeAndWrap(Parse cells) throws Exception {
        return wrapObjectWithFixture(invoke(cells));
    }
}
