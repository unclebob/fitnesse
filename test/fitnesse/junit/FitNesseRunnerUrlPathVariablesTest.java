package fitnesse.junit;

import org.junit.runner.RunWith;

@RunWith(FitNesseRunner.class)
@FitNesseRunner.Suite("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestScriptTable")
@FitNesseRunner.FitnesseDir(".")
@FitNesseRunner.OutputDir("./build/fitnesse-results")
@FitNesseRunner.UrlPathVariables("ID=100&gizmo=true")
public class FitNesseRunnerUrlPathVariablesTest {

  // this space intentionally left blank

}
