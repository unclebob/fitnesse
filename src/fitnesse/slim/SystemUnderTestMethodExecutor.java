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
    return findSystemUnderTest(methodName, instance, instance.getClass(), args);
  }

  private MethodExecutionResult findSystemUnderTest(String methodName, Object instance, Class<?> k, Object[] args) throws Throwable{
    Field[] fields = k.getDeclaredFields();
    for (Field field : fields) {
      if (isSystemUnderTest(field)) {
        Object systemUnderTest = field.get(instance);
        MethodExecutionResult res = findAndInvoke(methodName, args, systemUnderTest);
        if (res.hasResult()) {
          return res;
        }
      }
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

  private boolean isSystemUnderTest(Field field) {
    return "systemUnderTest".equals(field.getName())
        || field.getAnnotation(SystemUnderTest.class) != null;
  }
}
