package fitnesse.junit;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JUnitHelperExampleTest {
  JUnitHelper helper;
  private String[] expectedTestsWithSuiteFilter = new String[]{
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ErikPragtBug",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim"
  };
  private List<String> visitedPages;

  @Before
  public void prepare() {
    helper = new JUnitHelper(".",
      new File(System.getProperty("java.io.tmpdir"), "fitnesse").getAbsolutePath(), new TestRecordingListener());
    visitedPages = new LinkedList<>();
  }

  @Test
  public void assertTestPasses_RunsATestThroughFitNesseAndWeCanInspectTheResultUsingJavaFormatter() throws Exception {
    String testName = "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SystemUnderTestTest";
    helper.assertTestPasses(testName);
    assertEquals(1, visitedPages.size());
    assertEquals(testName, visitedPages.get(0));
  }

  @Test
  public void assertSuitePasses_appliesSuiteFilterIfDefined() throws Exception {
    String suiteName = "FitNesse.SuiteAcceptanceTests.SuiteSlimTests";
    helper.assertSuitePasses(suiteName, "testSuite");

    assertEquals(new HashSet<>(Arrays.asList(expectedTestsWithSuiteFilter)),
      new HashSet<>(visitedPages));

  }

  @Test
  public void helperWillFailTestsIfNoTestsAreExecuted() throws Exception{
    try{
      helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", "nonExistingFilter");

    }
    catch (AssertionError ae){
      assertTrue(ae.getMessage().startsWith("at least one test"));
    }

    assertEquals(new HashSet<String>(),
      new HashSet<>(visitedPages));

  }

  private class TestRecordingListener implements TestSystemListener {
    @Override
    public void testSystemStarted(TestSystem testSystem) {

    }

    @Override
    public void testOutputChunk(TestPage testPage, String output) {

    }

    @Override
    public void testStarted(TestPage testPage) {
      visitedPages.add(((WikiTestPage) testPage).getPath());

    }

    @Override
    public void testComplete(TestPage testPage, TestSummary testSummary) {

    }

    @Override
    public void testSystemStopped(TestSystem testSystem, Throwable cause) {

    }

    @Override
    public void testAssertionVerified(Assertion assertion, TestResult testResult) {

    }

    @Override
    public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {

    }
  }
}
