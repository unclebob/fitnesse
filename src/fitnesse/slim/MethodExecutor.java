package fitnesse.slim;


import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public abstract class MethodExecutor {

  public abstract MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable;

  protected MethodExecutionResult findAndInvoke(String methodName, Object[] args, Object instance) throws Throwable {
   FixtureInteraction interaction = SlimService.getInteraction();
   return interaction.findAndInvoke(methodName, instance, args);
  }
}