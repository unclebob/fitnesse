package fitnesse.slim;

import java.lang.reflect.Field;

public class SystemUnderTestMethodExecutor extends MethodExecutor {

  private final SlimExecutionContext context;

  public SystemUnderTestMethodExecutor(SlimExecutionContext context) {
    this.context = context;
  }

  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable {
    Object instance;
    try {
      instance = context.getInstance(instanceName);
    } catch (SlimError e) {
      return MethodExecutionResult.noInstance(instanceName + "." + methodName);
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
