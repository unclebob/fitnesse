package fitnesse.slim;

import java.lang.reflect.Field;

public class SystemUnderTestMethodExecutor extends MethodExecutor {

  public SystemUnderTestMethodExecutor(SlimExecutionContext context) {
    super(context);
  }

  @Override
  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable {
    Object instance;
    try {
      instance = context.getInstance(instanceName);
    } catch (SlimError e) {
      return MethodExecutionResult.noInstance(instanceName + "." + methodName);
    }
    Field field = findSystemUnderTest(methodName, instance, instance.getClass(), args);
    if (field != null) {
      Object systemUnderTest = field.get(instance);
      return findAndInvoke(methodName, args, systemUnderTest);
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

  private Field findSystemUnderTest(String methodName, Object instance, Class<?> k, Object[] args) throws Throwable{
    Field[] fields = k.getDeclaredFields();
    for (Field field : fields) {
      if (isSystemUnderTest(field)) {
        Object o = field.get(instance);
        Class type = o == null ? field.getType() : o.getClass();
        if (null != findMatchingMethod(methodName, type, args.length)) {
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
