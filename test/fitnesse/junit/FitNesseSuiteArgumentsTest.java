package fitnesse.junit;

import java.io.File;

import org.junit.Test;
import org.junit.runners.model.InitializationError;

import static org.junit.Assert.*;
public class FitNesseSuiteArgumentsTest {

  @Test
  public void argumentsAreParsedCorrectly() throws InitializationError{
    assertEquals(".", FitNesseSuite.getFitnesseDir(FitNesseSuiteExampleTest.class));
    assertEquals("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", FitNesseSuite.getSuiteName(FitNesseSuiteExampleTest.class));
    assertEquals("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", FitNesseSuite.getSuiteName(FitNesseSuiteExampleTest.class));
    assertEquals(new File(System.getProperty("java.io.tmpdir"),"fitnesse").getAbsolutePath(),FitNesseSuite.getOutputDir(FitNesseSuiteExampleTest.class));
    assertNull("null filter allowed", FitNesseSuite.getSuiteFilter(FitNesseSuiteExampleTest.class));
    assertNull("null exclude filter allowed", FitNesseSuite.getExcludeSuiteFilter(FitNesseSuiteExampleTest.class));
    assertEquals("testSuite", FitNesseSuite.getSuiteFilter(FitNesseSuiteWithFilterExampleTest.class));
    assertEquals("excludedSuite", FitNesseSuite.getExcludeSuiteFilter(FitNesseSuiteWithFilterExampleTest.class));
    assertEquals(true, FitNesseSuite.useDebugMode(FitNesseSuiteExampleTest.class));
    assertEquals(true, FitNesseSuite.useDebugMode(FitNesseSuiteWithFilterExampleTest.class));
    assertEquals(false, FitNesseSuite.useDebugMode(FitNesseSuiteExampleTestNoDebug.class));
  }
}
