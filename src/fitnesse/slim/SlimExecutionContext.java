package fitnesse.slim;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public class SlimExecutionContext {
    private Map<String, Object> instances = new HashMap<String, Object>();
    private List<Library> libraries = new ArrayList<Library>();
    private VariableStore variables = new VariableStore();
    private List<String> paths = new ArrayList<String>();

    public SlimExecutionContext() {
    }

    public List<Library> getLibraries() {
        return Collections.unmodifiableList(libraries);
    }

    public void addLibrary(Library library) {
        libraries.add(library);
    }

    public void setVariable(String name, MethodExecutionResult value) {
        variables.setSymbol(name, value);
    }

    public void setVariable(String name, Object value) {
        setVariable(name, new MethodExecutionResult(value, Object.class));
    }

    public void create(String instanceName, String className, Object[] args)
            throws SlimError, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        if (hasStoredActor(className)) {
            addToInstancesOrLibrary(instanceName, getStoredActor(className));
        } else {
            String replacedClassName = variables
                    .replaceSymbolsInString(className);
            Object instance = createInstanceOfConstructor(
                    replacedClassName, replaceSymbols(args));
            addToInstancesOrLibrary(instanceName, instance);
        }
    }

    public void addPath(String path) {
        if (!paths.contains(path)) {
            paths.add(path);
        }
    }

    public Object getInstance(String instanceName) {
        Object instance = instances.get(instanceName);
        if (instance != null) {
            return instance;
        }

        for (Library library : libraries) {
            if (library.instanceName.equals(instanceName)) {
                return library.instance;
            }
        }
        throw new SlimError(String.format("message:<<%s %s>>",
                SlimServer.NO_INSTANCE, instanceName));
    }

    private void addToInstancesOrLibrary(String instanceName, Object instance) {
        if (isLibrary(instanceName)) {
            libraries.add(new Library(instanceName, instance));
        } else {
            setInstance(instanceName, instance);
        }
    }

    public void setInstance(String instanceName, Object instance) {
        instances.put(instanceName, instance);
    }

    private boolean hasStoredActor(String nameWithDollar) {
        if (!variables.containsValueFor(nameWithDollar)) {
            return false;
        }
        Object potentialActor = getStoredActor(nameWithDollar);
        return potentialActor != null && !(potentialActor instanceof String);
    }

    private Object getStoredActor(String nameWithDollar) {
        return variables.getStored(nameWithDollar);
    }

    private boolean isLibrary(String instanceName) {
        return instanceName.startsWith("library");
    }

    private Object createInstanceOfConstructor(String className, Object[] args)
            throws IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        Class<?> k = searchPathsForClass(className);
        Constructor<?> constructor = getConstructor(k.getConstructors(), args);
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

        FixtureInteraction interaction = SlimService.getInteractionClass()
                .newInstance();
        return interaction.newInstance(constructor, initargs);
    }

    private Class<?> searchPathsForClass(String className) {
        Class<?> k = getClass(className);
        if (k != null) {
            return k;
        }
        List<String> reversedPaths = new ArrayList<String>(paths);
        Collections.reverse(reversedPaths);
        for (String path : reversedPaths) {
            k = getClass(path + "." + className);
            if (k != null) {
                return k;
            }
        }
        throw new SlimError(String.format("message:<<%s %s>>", SlimServer.NO_CLASS, className));
    }

    private Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Constructor<?> getConstructor(Constructor<?>[] constructors,
            Object[] args) {
        for (Constructor<?> constructor : constructors) {
            Class<?> arguments[] = constructor.getParameterTypes();
            if (arguments.length == args.length) {
                return constructor;
            }
        }
        return null;
    }

    public Object[] replaceSymbols(Object[] args) {
        return variables.replaceSymbols(args);
    }
}
