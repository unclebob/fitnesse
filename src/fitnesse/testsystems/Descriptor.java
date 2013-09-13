package fitnesse.testsystems;

import java.util.Map;

/**
 * Configuration for a test system.
 */
public interface Descriptor {
  String getTestSystem();

  String getTestSystemName();

  String getTestSystemType();

  String getTestRunner();

  String getCommandPattern();

  Map<String, String> createClasspathEnvironment(String classPath);

  String getClassPath();

  boolean isDebug();

  // Generic entry point for everything the test system needs to know.
  String getVariable(String name);
}
