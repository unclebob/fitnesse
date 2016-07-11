package fitnesse.junit;

import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoader;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testsystems.*;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(FitNesseRunnerExtensionTest.SuiteExtension.class)
@FitNesseRunner.FitnesseDir(".")
@FitNesseRunner.OutputDir("./build/fitnesse-results")
public class FitNesseRunnerExtensionTest {

  public static class SuiteExtension extends FitNesseRunner {
    private DescriptionFactory myDescriptionFactory = new DescriptionFactory();

    public SuiteExtension(Class<?> suiteClass) throws InitializationError {
      super(suiteClass);
      assertNotNull("No default description factory", getDescriptionFactory());
      setDescriptionFactory(myDescriptionFactory);
    }

    @Override
    protected void addTestSystemListeners(RunNotifier notifier,
                                          MultipleTestsRunner testRunner,
                                          Class<?> suiteClass,
                                          DescriptionFactory descriptionFactory) {
      assertEquals("Wrong description factory provided to listener", myDescriptionFactory, descriptionFactory);
      testRunner.addTestSystemListener(new ListenerExtension(notifier, suiteClass, descriptionFactory));
    }

    @Override
    protected String getSuiteName(Class<?> klass) throws InitializationError {
      return "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestScriptTable";
    }

    @Override
    protected FitNesseContext createContext(Class<?> suiteClass) throws Exception {
      new PluginsClassLoader(getFitNesseRoot(suiteClass)).addPluginsToClassLoader();

      return super.createContext(suiteClass);
    }
  }

  public static class ListenerExtension extends JUnitRunNotifierResultsListener {
    public ListenerExtension(RunNotifier notifier, Class<?> mainClass, DescriptionFactory descriptionFactory) {
      super(notifier, mainClass, descriptionFactory);
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
      super.announceNumberTestsToRun(testsToRun);
    }

    @Override
    public void unableToStartTestSystem(String testSystemName, Throwable cause) {
      super.unableToStartTestSystem(testSystemName, cause);
    }

    @Override
    public void testStarted(TestPage test) {
      super.testStarted(test);
    }

    @Override
    public void testComplete(TestPage test, TestSummary testSummary) {
      super.testComplete(test, testSummary);
    }

    @Override
    public void testSystemStopped(TestSystem testSystem, Throwable cause) {
      super.testSystemStopped(testSystem, cause);
    }
  }
}
