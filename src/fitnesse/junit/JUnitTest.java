package fitnesse.junit;

import java.io.File;
import java.io.IOException;

import fitnesse.PluginException;
import fitnesse.components.PluginsClassLoader;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(JUnitTest.SuiteExtension.class)
@FitNesseSuite.FitnesseDir(".")
@FitNesseSuite.OutputDir("../target/fitnesse-results")
public class JUnitTest {

  public static class SuiteExtension extends FitNesseSuite {
    public SuiteExtension(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError, IOException, PluginException {
      super(suiteClass, builder);
    }

    @Override
    protected String getSuiteName(Class<?> klass) throws InitializationError {
      return "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestScriptTable";
    }

    @Override
    protected void beforeContextCreated(File configFile, String rootPath, String fitNesseRoot, int port) throws InitializationError {
      try {
        new PluginsClassLoader(rootPath).addPluginsToClassLoader();
      } catch (Exception e) {
        throw new InitializationError(e);
      }
      super.beforeContextCreated(configFile, rootPath, fitNesseRoot, port);
    }
  }
}
