package fitnesse.testrunner;

import fitnesse.testsystems.TestSystemFactory;

public interface TestSystemFactoryRegistrar {
  void registerTestSystemFactory(String name, TestSystemFactory testSystemFactory);
}
