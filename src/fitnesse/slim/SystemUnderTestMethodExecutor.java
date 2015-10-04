package fitnesse.slim;

import java.lang.reflect.Field;

public class SystemUnderTestMethodExecutor extends MethodExecutor {

  private final SlimExecutionContext context;

  public SystemUnderTestMethodExecutor(SlimExecutionContext context) {
    this.context = context;
  }

  @Override
  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable {
    Object instance;
    try {
      instance = context.getInstance(instanceName);
    } catch (SlimError e) {
      return MethodExecutionResult.noInstance(instanceName + "." + methodName);
    }
    Field field = findSystemUnderTest(methodName, instance.getClass(), args);
    if (field != null) {
      Object systemUnderTest = field.get(instance);
      return findAndInvoke(methodName, args, systemUnderTest);
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

  private Field findSystemUnderTest(String methodName, Class<?> k, Object[] args) {
    Field[] fields = k.getDeclaredFields();
    for (Field field : fields) {
      if (isSystemUnderTest(field)) {
        if (null != findMatchingMethod(methodName, field.getType(), args.length)) {
          return field;
        }
      }
    }
    return null;
  }

  private boolean isSystemUnderTest(Field field) {
    return "systemUnderTest".equals(field.getName())
        || field.getAnnotation(SystemUnderTest.class) != null;
  }
}
