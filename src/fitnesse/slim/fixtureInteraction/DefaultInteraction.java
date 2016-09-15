package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import fitnesse.slim.ConverterSupport;
import fitnesse.slim.MethodExecutionResult;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;

public class DefaultInteraction implements FixtureInteraction {
  private static final Method AROUND_METHOD;

  static {
    try {
      AROUND_METHOD = InteractionAwareFixture.class.getMethod("aroundSlimInvoke", FixtureInteraction.class, Method.class, Object[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

	@Override
	public Object createInstance(List<String> paths, String className, Object[] args)
			throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Class<?> k = searchPathsForClass(paths, className);
		Constructor<?> constructor = getConstructor(k, args);
		if (constructor == null) {
			throw new SlimError(String.format("message:<<%s %s>>",
					SlimServer.NO_CONSTRUCTOR, className));
		}

		return newInstance(args, constructor);
	}

	private Object newInstance(Object[] args, Constructor<?> constructor)
			throws IllegalAccessException, InstantiationException,
			InvocationTargetException {
		Object[] initargs = ConverterSupport.convertArgs(args,
				constructor.getParameterTypes());

		return newInstance(constructor, initargs);
	}


	protected Class<?> searchPathsForClass(List<String> paths, String className) {
		Class<?> k = getClass(className);

		if (k != null) {
			return k;
		}
		for (String path : paths) {
			k = getClass(path + "." + className);
			if (k != null) {
				return k;
			}
		}
		throw new SlimError(String.format("message:<<%s %s>>", SlimServer.NO_CLASS, className));
	}

	protected Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	protected Constructor<?> getConstructor(Class<?> clazz,
			Object[] args) {

		for (Constructor<?> constructor : clazz.getConstructors()) {
			Class<?>[] arguments = constructor.getParameterTypes();
			if (arguments.length == args.length) {
				return constructor;
			}
		}
		return null;
	}

	protected Object newInstance(Constructor<?> constructor, Object... initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		return constructor.newInstance(initargs);
	}

	@Override
	public MethodExecutionResult findAndInvoke(String methodName, Object instance, Object... args) throws Throwable {
		Method method = findMatchingMethod(methodName, instance, args);
		if (method != null) {
			return this.invokeMethod(instance, method, args);
		}
		return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
	}

	protected Method findMatchingMethod(String methodName, Object instance, Object... args) {
    Class<?> k = instance.getClass();
		Method[] methods = k.getMethods();

    int nArgs = args.length;
		for (Method method : methods) {
			boolean hasMatchingName = method.getName().equals(methodName);
			boolean hasMatchingArguments = method.getParameterTypes().length == nArgs;
			if (hasMatchingName && hasMatchingArguments) {
				return method;
			}
		}
		return null;
	}

	protected MethodExecutionResult invokeMethod(Object instance, Method method, Object[] args) throws Throwable {
		Object[] convertedArgs = convertArgs(method, args);
		Object retval = callMethod(instance, method, convertedArgs);
		Class<?> retType = method.getReturnType();
		return new MethodExecutionResult(retval, retType);
	}

	protected Object[] convertArgs(Method method, Object[] args) {
		Type[] argumentParameterTypes = method.getGenericParameterTypes();
		return ConverterSupport.convertArgs(args, argumentParameterTypes);
	}

	protected Object callMethod(Object instance, Method method, Object[] convertedArgs) throws Throwable {
		try {
			Object result;
			if (instance instanceof InteractionAwareFixture) {
				// invoke via interaction, so it can also do its thing on the aroundMethod invocation
				Object[] args = { this, method, convertedArgs };
        result = methodInvoke(AROUND_METHOD, instance, args);
			} else {
				result = methodInvoke(method, instance, convertedArgs);
			}
			return result;
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				throw e.getCause();
			} else {
				throw e.getTargetException();
			}
		}
	}

  @Override
	public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws Throwable {
		try {
			return method.invoke(instance, convertedArgs);
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				throw e.getCause();
			} else {
				throw e.getTargetException();
			}
		} catch (IllegalArgumentException e) {
		  throw new RuntimeException("Bad call of: " + method.getDeclaringClass().getName() + "." + method.getName()
                                  + ". On instance of: " + instance.getClass().getName(), e);
    }
	}
}
