package fitnesse.junit;


import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;

import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JUnitHelper {

  private final TestHelper helper;
  private int port = 0;

  public void setPort(int port) {
    this.port = port;
  }

  public JUnitHelper(String fitNesseRootPath, String outputPath) {
    this(fitNesseRootPath, outputPath, new PrintTestListener());
  }

  public JUnitHelper(String fitNesseDir, String outputDir,
                     TestSystemListener resultsListener) {
    helper = new TestHelper(fitNesseDir, outputDir, resultsListener);
  }

  public void setDebugMode(boolean enabled) {
    helper.setDebugMode(enabled);
  }

  public void assertTestPasses(String testName) throws Exception {
    assertPasses(testName, TestHelper.PAGE_TYPE_TEST, null);
  }

  public void assertSuitePasses(String suiteName) throws Exception {
    assertPasses(suiteName, TestHelper.PAGE_TYPE_SUITE, null);
  }

  public void assertSuitePasses(String suiteName, String suiteFilter) throws Exception {
    assertPasses(suiteName, TestHelper.PAGE_TYPE_SUITE, suiteFilter);
  }

  public void assertSuitePasses(String suiteName, String suiteFilter, String excludeSuiteFilter) throws Exception {
    assertPasses(suiteName, TestHelper.PAGE_TYPE_SUITE, suiteFilter, excludeSuiteFilter);
  }

  public void assertPasses(String pageName, String pageType, String suiteFilter) throws Exception {
    assertPasses(pageName, pageType, suiteFilter, null);
  }

  public void assertPasses(String pageName, String pageType, String suiteFilter, String excludeSuiteFilter) throws Exception {
    TestSummary summary = helper.run(pageName, pageType, suiteFilter, excludeSuiteFilter, port);
    assertEquals("wrong", 0, summary.wrong);
    assertEquals("exceptions", 0, summary.exceptions);
    assertTrue(msgAtLeastOneTest(pageName, summary), summary.right > 0);
  }

  private String msgAtLeastOneTest(String pageName, TestSummary summary) {
    return
      MessageFormat.format("at least one test executed in {0}\n{1}",
        pageName, summary.toString());
  }
}
