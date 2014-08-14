package fitnesse.junit;

import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoader;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

@RunWith(FitNesseRunnerExtensionTest.SuiteExtension.class)
@FitNesseRunner.FitnesseDir(".")
@FitNesseRunner.OutputDir("../target/fitnesse-results")
public class FitNesseRunnerExtensionTest {

  public static class SuiteExtension extends FitNesseRunner {
    public SuiteExtension(Class<?> suiteClass) throws InitializationError {
      super(suiteClass);
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
}
