package fitnesse.testsystems;

import java.util.Map;

/**
 * Configuration for a test system.
 */
public interface Descriptor {
  String getTestSystem();

  String getTestSystemType();

  String getClassPath();

  boolean runInProcess();

  boolean isDebug();

  // Generic entry point for everything the test system needs to know.
  String getVariable(String name);
}
