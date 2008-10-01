package fitnesse.slim;

import fitnesse.slim.converters.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * This is the API for executing a SLIM statement.  This class should not know about
 * the syntax of a SLIM statement.
 */

public class StatementExecutor {
  private Map<String, Object> instances = new HashMap<String, Object>();
  private Map<Class, Converter> converters = new HashMap<Class, Converter>();
  private Map<String, Object> variables = new HashMap<String, Object>();
  private List<String> paths = new ArrayList<String>();

  public StatementExecutor() {
    addConverter(void.class, new VoidConverter());
    addConverter(String.class, new StringConverter());
    addConverter(int.class, new IntConverter());
    addConverter(double.class, new DoubleConverter());
    addConverter(Integer.class, new IntConverter());
    addConverter(Double.class, new DoubleConverter());
    addConverter(char.class, new CharConverter());
  }

  public void setVariable(String name, Object value) {
    variables.put(name, value);
  }

  public Object addPath(String path) {
    paths.add(path);
    return "OK";
  }

  public Object getInstance(String instanceName) {
    Object instance = instances.get(instanceName);
    if (instance != null)
      return instance;
    throw new SlimError(String.format("No such instance: %s.", instanceName));
  }

  public void addConverter(Class k, Converter converter) {
    converters.put(k, converter);
  }

  public Converter getConverter(Class k) {
    return converters.get(k);
  }

  public Object create(String instanceName, String className, Object[] args) {
    try {
      Object instance = createInstanceOfConstructor(className, replaceVariables(args));
      instances.put(instanceName, instance);
      return "OK";
    } catch (Throwable e) {
      return exceptionToString(new SlimError("Could not invoke constructor.", e));
    }
  }


  private Object createInstanceOfConstructor(String className, Object[] args) throws Exception {
    Class k = searchPathsForClass(className);
    Constructor constructor = getConstructor(k.getConstructors(), args);
    if (constructor == null)
      throw new SlimError(String.format("Class %s has no appropriate constructor.", className));

    return constructor.newInstance(convertArgs(args, constructor.getParameterTypes()));
  }

  private Class searchPathsForClass(String className) {
    Class k = getClass(className);
    if (k != null)
      return k;
    for (String path : paths) {
      k = getClass(path + "." + className);
      if (k != null)
        return k;
    }
    throw new SlimError(String.format("Class %s not found.", className));
  }

  private Class getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private Constructor getConstructor(Constructor[] constructors, Object[] args) {
    for (Constructor constructor : constructors) {
      Class arguments[] = constructor.getParameterTypes();
      if (arguments.length == args.length)
        return constructor;
    }
    return null;
  }

  public Object call(String instanceName, String methodName, Object... args) {
    try {
      Object instance = getInstance(instanceName);
      return tryToInvokeMethod(instance, methodName, replaceVariables(args));
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private String exceptionToString(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    e.printStackTrace(pw);
    return SlimServer.EXCEPTION_TAG + stringWriter.toString();
  }

  private Object[] replaceVariables(Object[] args) {
    Object result[] = new Object[args.length];
    for (int i = 0; i < args.length; i++)
      result[i] = replaceVariable(args[i]);
    
    return result;
  }

  private List<Object> replaceArgsInList(List<Object> objects) {
    List<Object> result = new ArrayList<Object>();
    for (Object object : objects)
      result.add(replaceVariable(object));

    return result;
  }

  private Object replaceVariable(Object object) {
    if (object instanceof List)
      return (replaceArgsInList((List<Object>) object));
    else
      return (replaceIfVariable((String) object));
  }

  private Object replaceIfVariable(String arg) {
    if (isVariableReference(arg))
      return replaceVariable(arg);
    return arg;
  }

  private Object replaceVariable(String arg) {
    String varName = arg.substring(1);
    if (variables.containsKey(varName))
      return variables.get(varName);
    return arg;
  }

  private boolean isVariableReference(String arg) {
    return arg.charAt(0) == '$';
  }

  private Object tryToInvokeMethod(Object instance, String methodName, Object args[]) throws Exception {
    Class k = instance.getClass();
    Method method = findMatchingMethod(methodName, k, args.length);
    Object convertedArgs[] = convertArgs(method, args);
    Object retval = method.invoke(instance, convertedArgs);
    Class retType = method.getReturnType();
    if (retType == List.class && retval instanceof List)
      return retval;
    return convertToString(retval, retType);
  }

  private Method findMatchingMethod(String methodName, Class k, int nArgs) {
    Method methods[] = k.getMethods();

    for (Method method : methods) {
      boolean hasMatchingName = method.getName().equals(methodName);
      boolean hasMatchingArguments = method.getParameterTypes().length == nArgs;
      if (hasMatchingName && hasMatchingArguments)
        return method;
    }
    throw new SlimError(String.format("Method %s(%d) not found in %s.", methodName, nArgs, k.getName()));
  }

  private Object[] convertArgs(Method method, Object args[]) {
    Class[] argumentTypes = method.getParameterTypes();
    Object[] convertedArgs = convertArgs(args, argumentTypes);
    return convertedArgs;
  }

  private Object[] convertArgs(Object[] args, Class[] argumentTypes) {
    Object[] convertedArgs = new Object[args.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      Class argumentType = argumentTypes[i];
      if (argumentType == List.class && args[i] instanceof List) {
        convertedArgs[i] = args[i];
      } else {
        Converter converter = getConverter(argumentType);
        if (converter != null)
          convertedArgs[i] = converter.fromString((String) args[i]);
        else
          throw new SlimError(String.format("No converter for argument: %d -- %s", i, argumentType.getName()));
      }
    }
    return convertedArgs;
  }

  private Object convertToString(Object retval, Class retType) {
    Converter converter = getConverter(retType);
    if (converter != null)
      return converter.toString(retval);
    else return retval.toString();
  }
}
