package fitnesse.testsystems;

import java.util.Map;

/**
 * Configuration for a test system.
 */
public interface Descriptor {
  String getTestSystemName();

  String getTestSystemType();

  // To ClientBuilder
  String getTestRunner();

  // To ClientBuilder
  String getCommandPattern();

  // To ClientBuilder
  Map<String, String> createClasspathEnvironment(String classPath);

  // To ClientBuilder
  String getClassPath();

  boolean isDebug();

  // Generic entry point for everything the test system needs to know.
  String getVariable(String name);
}
