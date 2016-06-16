package fitnesse.slim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public class SlimExecutionContext {
    private final FixtureInteraction interaction;
    private Map<String, Object> instances = new HashMap<>();
    private List<Library> libraries = new ArrayList<>();
    private VariableStore variables = new VariableStore();
    private List<String> paths = new ArrayList<>();

    public SlimExecutionContext(FixtureInteraction interaction) {
      this.interaction = interaction;
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

    public MethodExecutionResult getVariable(String name) {
        return variables.getSymbol(name);
    }

    public void setVariable(String name, Object value) {
        setVariable(name, new MethodExecutionResult(value, Object.class));
    }

    public void create(String instanceName, String className, Object[] args)
            throws SlimError, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
      Object potentialActor = variables.getStored(className);
      if (potentialActor != null && !(potentialActor instanceof String)) {
          addToInstancesOrLibrary(instanceName, potentialActor);
        } else {
            String replacedClassName = variables
                    .replaceSymbolsInString(className);
            Object instance = interaction.createInstance(paths,
                    replacedClassName, replaceSymbols(args));
            addToInstancesOrLibrary(instanceName, instance);
        }
    }

    public void addPath(String path) {
        if (!paths.contains(path)) {
            paths.add(0, path);
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

    private boolean isLibrary(String instanceName) {
        return instanceName.startsWith("library");
    }

    public Object[] replaceSymbols(Object[] args) {
        return variables.replaceSymbols(args);
    }

  public FixtureInteraction getInteraction() {
    return interaction;
  }
}
