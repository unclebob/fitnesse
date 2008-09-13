package fitnesse.slim;

import fitnesse.slim.converters.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the API for executing a SLIM statement.  This class should not know about
 * the syntax of a SLIM statement.
 */

public class StatementExecutor {
  private Map<String, Object> instances = new HashMap<String, Object>();
  private Map<Class, Converter> converters = new HashMap<Class, Converter>();
  private Map<String, String> variables = new HashMap<String, String>();
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

  public void setVariable(String name, String value) {
    variables.put(name, value);
  }

  public void addPath(String path) {
    paths.add(path);
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

  public Object create(String instanceName, String className) {
    Object instance = tryToCreateInstanceOfDefaultConstructor(className);
    instances.put(instanceName, instance);
    return instance;
  }

  private Object tryToCreateInstanceOfDefaultConstructor(String className) {
    try {
      return createInstanceOfDefaultConstructor(className);
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }

  private Object createInstanceOfDefaultConstructor(String className) throws Exception {
    Class k = searchPathsForClass(className);
    Constructor defaultConstructor = getDefaultConstructor(k.getConstructors());
    if (defaultConstructor == null)
      throw new SlimError(String.format("Class %s has no default constructor.", className));
    return defaultConstructor.newInstance();
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
    throw new SlimError(String.format("Class %s found.", className));
  }

  private Class getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private Constructor getDefaultConstructor(Constructor[] constructors) {
    for (Constructor constructor : constructors) {
      Class arguments[] = constructor.getParameterTypes();
      if (arguments.length == 0)
        return constructor;
    }
    return null;
  }

  public String call(String instanceName, String methodName, String... args) {
    Object instance = getInstance(instanceName);
    try {
      return tryToInvokeMethod(instance, methodName, replaceVariables(args));
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }

  private String[] replaceVariables(String[] args) {
    String result[] = new String[args.length];
    for (int i = 0; i < args.length; i++)
      result[i] = replaceIfVariable(args[i]);

    return result;
  }

  private String replaceIfVariable(String arg) {
    if (isVariableReference(arg))
      return replaceVariable(arg);
    return arg;
  }

  private String replaceVariable(String arg) {
    String varName = arg.substring(1);
    if (variables.containsKey(varName))
      return variables.get(varName);
    return arg;
  }

  private boolean isVariableReference(String arg) {
    return arg.charAt(0) == '$';
  }

  private String tryToInvokeMethod(Object instance, String methodName, String args[]) throws Exception {
    Class k = instance.getClass();
    Method method = findMatchingMethod(methodName, k, args.length);
    Object convertedArgs[] = convertArgs(method, args);
    Object retval = method.invoke(instance, convertedArgs);
    Class retType = method.getReturnType();
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

  private Object[] convertArgs(Method method, String args[]) {
    Object[] convertedArgs = new Object[args.length];
    Class[] argumentTypes = method.getParameterTypes();
    for (int i = 0; i < argumentTypes.length; i++) {
      Class argumentType = argumentTypes[i];
      Converter converter = getConverter(argumentType);
      if (converter != null)
        convertedArgs[i] = converter.fromString(args[i]);
      else
        throw new SlimError(String.format("No converter for argument: %d -- %s", i, argumentType.getName()));
    }
    return convertedArgs;
  }

  private String convertToString(Object retval, Class retType) {
    Converter converter = getConverter(retType);
    if (converter != null)
      return converter.toString(retval);
    else return retval.toString();
  }

  public List<Object> describeClass(String className) {
    List<Object> description = new ArrayList<Object>();

    Class k = searchPathsForClass(className);
    List<String> methods = getMethods(k);
    List<String> variables = getVariables(k);
    description.add(variables);
    description.add(methods);
    return description;
  }

  private List<String> getVariables(Class k) {
    List<String> variableNames = new ArrayList<String>();
    Field fields[] = k.getFields();
    for (Field field : fields)
      variableNames.add(field.getName());
    return variableNames;
  }

  private List<String> getMethods(Class k) {
    List<String> methodNames = new ArrayList<String>();
    Method methods[] = k.getMethods();
    for (Method method : methods) {
      methodNames.add(String.format("%s(%d)", method.getName(), method.getParameterTypes().length));
    }
    return methodNames;
  }

  public Object set(String instanceName, String variableName, String value) {
    new FieldAccessor(instanceName, variableName).set(value);
    return null;
  }

  public Object get(String instanceName, String variableName) {
    return new FieldAccessor(instanceName, variableName).get();
  }

  class FieldAccessor {
    private String instanceName;
    private String variableName;
    private final Object instance;
    private final Field field;
    private final Converter converter;

    public FieldAccessor(String instanceName, String variableName) {
      this.instanceName = instanceName;
      this.variableName = variableName;
      instance = getInstance(instanceName);
      Class aClass = instance.getClass();
      field = getField(aClass, variableName);
      converter = getConverter(field.getType());
    }

    public void set(String value) {
      try {
        field.set(instance, converter.fromString(value));
      } catch (IllegalAccessException e) {
        throw makeAccessException();
      }
    }

    private Field getField(Class aClass, String variableName) {
      try {
        return aClass.getField(variableName);
      } catch (NoSuchFieldException e) {
        throw new SlimError(String.format("Variable %s.%s does not exist", aClass.getName(), variableName)
        );
      }
    }

    private SlimError makeAccessException() {
      return new SlimError(String.format("Cannot access: %s.%s.", instanceName, variableName));
    }

    public Object get() {
      try {
        return converter.toString(field.get(instance));
      } catch (IllegalAccessException e) {
        throw makeAccessException();
      }
    }
  }
}
