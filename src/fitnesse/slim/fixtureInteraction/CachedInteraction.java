package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CachedInteraction extends DefaultInteraction {
	private static class NotExisting {}
	private static Constructor<?> noConstructor = NotExisting.class.getConstructors()[0];

	private Map<String, Constructor<?>> constructorsByClassAndArgs = new HashMap<>();
	private Map<String, Class<?>> classCache = new HashMap<>();
	private Map<MethodKey, Method> methodsByNameAndArgs = new HashMap<>();

	protected Constructor<?> getConstructor(Class<?> clazz,
			Object[] args) {
		String key = String.format("%s_%d", clazz.getName(), args.length);
		Constructor<?> cached = constructorsByClassAndArgs.get(key);
		if(cached == noConstructor) return null;
		if(cached != null) return cached;
		
		Constructor<?> constructor = super.getConstructor(clazz, args);
		if(constructor == null) {			
			constructorsByClassAndArgs.put(key, noConstructor);
		} else {
			constructorsByClassAndArgs.put(key, constructor);
		}
		return constructor;
	}
	

	@Override
	protected Class<?> getClass(String className) {
		Class<?> k = classCache.get(className);
		if(k == NotExisting.class) return null;
		if(k != null) return k;

		k = super.getClass(className);
		if(k == null) {
			classCache.put(className, NotExisting.class);
		} else {
			classCache.put(className, k);
		}
		return k;
	}

	
	private static class MethodKey {
		String k;
		String method;
		int nArgs;
		public MethodKey(Class<?> k, String method, int nArgs) {
			this.k = k.getSimpleName();
			this.method = method;
			this.nArgs = nArgs;
		}

		public int hashCode() {
			return nArgs * 31 + method.hashCode() + 31 * k.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof MethodKey)) return false;
			MethodKey m = (MethodKey) o;
			if(m.k != k) return false;
			if(m.nArgs != nArgs) return false;
			if(!m.method.equals(method)) return false;
			return true;
		}
	}

	@Override
	protected Method findMatchingMethod(String methodName, Class<?> k, int nArgs) {
		MethodKey key = new MethodKey(k, methodName, nArgs);
		Method cached = this.methodsByNameAndArgs.get(key);
		if(cached != null) return cached;

		Method method = super.findMatchingMethod(methodName, k, nArgs);
		
		this.methodsByNameAndArgs.put(key, method);
		return method;
	}
}
