package fitnesse.junit;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.*;
public class FitNesseSuiteArgumentsTest {

  @Test
  public void argumentsAreParsedCorrectly() throws Exception {
	System.setProperty("fitnesse.root.dir.parent", ".");
    FitNesseSuite suite = new FitNesseSuite(FitNesseSuiteTest.class);
    assertEquals(".", suite.getFitNesseDir(FitNesseSuiteExampleTest.class));
    assertEquals(new File(System.getProperty("fitnesse.root.dir.parent")).getAbsolutePath(), suite.getFitNesseDir(FitNesseSuiteExampleFromPropertiesTest.class));
    assertEquals("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TableTableSuite", suite.getSuiteName(FitNesseSuiteExampleTest.class));
    assertEquals("tmp",suite.getOutputDir(FitNesseSuiteExampleTest.class));
    assertEquals(new File(System.getProperty("java.io.tmpdir"),"fitnesse").getAbsolutePath(),suite.getOutputDir(FitNesseSuiteExampleFromPropertiesTest.class));
    assertNull("null filter allowed", suite.getSuiteFilter(FitNesseSuiteExampleTest.class));
    assertNull("null exclude filter allowed", suite.getExcludeSuiteFilter(FitNesseSuiteExampleTest.class));
    assertEquals("testSuite", suite.getSuiteFilter(FitNesseSuiteWithFilterExampleTest.class));
    assertEquals("excludedSuite", suite.getExcludeSuiteFilter(FitNesseSuiteWithFilterExampleTest.class));
    assertEquals(true, suite.useDebugMode(FitNesseSuiteExampleTest.class));
    assertEquals(true, suite.useDebugMode(FitNesseSuiteWithFilterExampleTest.class));
    assertEquals(false, suite.useDebugMode(FitNesseSuiteExampleTestNoDebug.class));
  }
}
