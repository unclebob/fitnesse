package fitnesse.junit;

import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoader;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@RunWith(FitNesseRunnerExtensionTest.SuiteExtension.class)
@FitNesseRunner.FitnesseDir(".")
@FitNesseRunner.OutputDir("../target/fitnesse-results")
public class FitNesseRunnerExtensionTest {

  public static class SuiteExtension extends FitNesseRunner {
    public SuiteExtension(Class<?> suiteClass) throws InitializationError {
      super(suiteClass);
    }

    @Override
    protected void addTestSystemListeners(RunNotifier notifier, MultipleTestsRunner testRunner, Class<?> suiteClass) {
      testRunner.addTestSystemListener(new ListenerExtension(notifier, suiteClass));
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
    public ListenerExtension(RunNotifier notifier, Class<?> mainClass) {
      super(notifier, mainClass);
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
      super.announceNumberTestsToRun(testsToRun);
    }

    @Override
    public void unableToStartTestSystem(String testSystemName, Throwable cause) throws IOException {
      super.unableToStartTestSystem(testSystemName, cause);
    }

    @Override
    public void testStarted(WikiTestPage test) {
      super.testStarted(test);
    }

    @Override
    public void testComplete(WikiTestPage test, TestSummary testSummary) {
      super.testComplete(test, testSummary);
    }

    @Override
    public void testSystemStopped(TestSystem testSystem, Throwable cause) {
      super.testSystemStopped(testSystem, cause);
    }
  }
}
