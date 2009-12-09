package fitnesse.slim;

import java.lang.reflect.Field;
import java.util.Map;

public class SystemUnderTestMethodExecutor extends MethodExecutor {
  private final Map<String, Object> instances;

  public SystemUnderTestMethodExecutor(Map<String, Object> instances) {
    this.instances = instances;
  }

  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable {
    Object instance = instances.get(instanceName);
    if (instance == null) {
      return MethodExecutionResult.noInstance(instanceName);
    }
    Field field = findSystemUnderTest(instance.getClass());
    if (field != null) {
      Object systemUnderTest = field.get(instance);
      return findAndInvoke(methodName, args, systemUnderTest);
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

  private Field findSystemUnderTest(Class<?> k) {
    Field[] fields = k.getDeclaredFields();
    for (Field field : fields) {
      if (isSystemUnderTest(field)) {
        return field;
      }
    }
    return null;
  }

  private boolean isSystemUnderTest(Field field) {
    return "systemUnderTest".equals(field.getName())
        || field.getAnnotation(SystemUnderTest.class) != null;
  }
}
