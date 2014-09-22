package fitnesse.testsystems;

/**
 * Configuration for a test system.
 */
public interface Descriptor {
  String getTestSystem();

  String getTestSystemType();

  ClassPath getClassPath();

  boolean runInProcess();

  boolean isDebug();

  // Generic entry point for everything the test system needs to know.
  String getVariable(String name);

  ExecutionLogListener getExecutionLogListener();
}
