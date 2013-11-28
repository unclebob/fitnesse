package fitnesse.fixtures;

import fitnesse.testrunner.TestSystemFactoryRegistrar;
import fitnesse.testsystems.TestSystemFactory;

public class AddTestSystem {

  public AddTestSystem(String name, String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    TestSystemFactory factory = (TestSystemFactory) Class.forName(className).newInstance();
    ((TestSystemFactoryRegistrar)FitnesseFixtureContext.context.testSystemFactory).registerTestSystemFactory(name, factory);
  }
}
