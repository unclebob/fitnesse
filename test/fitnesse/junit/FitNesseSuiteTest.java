package fitnesse.junit;

import org.junit.runner.RunWith;

/**
 * Tests backwards compatibility so FitNesseSuite can still be used.
 */
@Deprecated
@RunWith(FitNesseSuite.class)
@FitNesseSuite.Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestScriptTable")
@FitNesseSuite.FitnesseDir(".")
@FitNesseSuite.OutputDir("./build/fitnesse-results")
public class FitNesseSuiteTest {
}
