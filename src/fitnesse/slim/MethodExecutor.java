package fitnesse.slim;


import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public abstract class MethodExecutor {

  protected final SlimExecutionContext context;

  public MethodExecutor(SlimExecutionContext context) {
    this.context = context;
  }

  public abstract MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable;

  protected MethodExecutionResult findAndInvoke(String methodName, Object[] args, Object instance) throws Throwable {
    FixtureInteraction interaction = context.getInteraction();
    return interaction.findAndInvoke(methodName, instance, args);
  }
}
