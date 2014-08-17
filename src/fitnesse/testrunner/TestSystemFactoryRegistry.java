package fitnesse.testrunner;

import fitnesse.testsystems.TestSystemFactory;

public interface TestSystemFactoryRegistry {
  void registerTestSystemFactory(String name, TestSystemFactory testSystemFactory);
}
