package fitnesse.fixtures;

import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.TestSystemFactory;

public class AddTestSystem {

  public AddTestSystem(String name, String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    TestSystemFactory factory = (TestSystemFactory) Class.forName(className).newInstance();
    ((TestSystemFactoryRegistry)FitnesseFixtureContext.context.testSystemFactory).registerTestSystemFactory(name, factory);
  }
}
