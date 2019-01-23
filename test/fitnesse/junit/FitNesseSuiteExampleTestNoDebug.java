package fitnesse.junit;

import org.junit.runner.RunWith;

@RunWith(FitNesseRunner.class)
@FitNesseRunner.Suite("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestSystemSlimSuite")
@FitNesseRunner.FitnesseDir(".")
@FitNesseRunner.OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@FitNesseRunner.DebugMode(false)
@FitNesseRunner.ExcludeSuiteFilter("noJunit")

public class FitNesseSuiteExampleTestNoDebug {
}
