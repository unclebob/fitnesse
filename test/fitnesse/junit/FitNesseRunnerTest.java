package fitnesse.junit;

import org.junit.runner.RunWith;

@RunWith(FitNesseRunner.class)
@FitNesseRunner.Suite("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestScriptTable")
@FitNesseRunner.FitnesseDir(".")
@FitNesseRunner.OutputDir("./build/fitnesse-results")
public class FitNesseRunnerTest {
}
