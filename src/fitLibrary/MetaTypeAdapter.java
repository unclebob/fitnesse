/*
 * @author Rick Mugridge on Jan 10, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import fit.TypeAdapter;
import fit.exception.FitFailureException;

/**
 *
 */
public class MetaTypeAdapter extends TypeAdapter {
    protected Object callReflectively(String methodName, Object[] args, 
            Class[] argTypes, Object object) {
        try {
            Method method = type.getMethod(methodName, argTypes);
            return method.invoke(object, args);
        } catch (SecurityException e) {
            error(methodName, argTypes, e);
        } catch (NoSuchMethodException e) {
            error(methodName, argTypes, e);
        } catch (IllegalArgumentException e) {
            error(methodName, argTypes, e);
        } catch (IllegalAccessException e) {
            error(methodName, argTypes, e);
        } catch (InvocationTargetException e) {
            error(methodName, argTypes, e.getTargetException());
        }
        return null; // satisfy compiler, as unreachable
    }

    private void error(String methodName, Class[] argTypes, Throwable ex) throws FitFailureException {
        String args = Arrays.asList(argTypes).toString();
        args = "("+args.substring(1,args.length()-1)+")";
        String problem = "Problem with accessing "+methodName+args+
                " of class "+type.getName()+": "+ex;
//        System.err.println(problem);
//        ex.printStackTrace();
        throw new FitFailureException(problem);
    }
}
