package fitnesse.junit;


import junit.framework.Assert;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;

public class JUnitHelper {
  
  private final TestHelper helper;
  public JUnitHelper(String fitNesseRootPath, String outputPath) {
    this(fitNesseRootPath,outputPath,new PrintTestListener());
  }
  public JUnitHelper(String fitNesseDir, String outputDir,
      ResultsListener resultsListener) {
      helper=new TestHelper(fitNesseDir, outputDir, resultsListener);
  }
  
  public void setDebugMode(boolean enabled) {
    helper.setDebugMode(enabled);
  }
  
  public void assertTestPasses(String testName) throws Exception{
    assertPasses(testName, TestHelper.PAGE_TYPE_TEST,null);
  }
  public void assertSuitePasses(String suiteName) throws Exception{
    assertPasses(suiteName, TestHelper.PAGE_TYPE_SUITE,null);
  }
  public void assertSuitePasses(String suiteName, String suiteFilter) throws Exception{
    assertPasses(suiteName, TestHelper.PAGE_TYPE_SUITE, suiteFilter);
  }
  public void assertPasses(String pageName, String pageType, String suiteFilter) throws Exception{
    TestSummary summary=helper.run(pageName, pageType, suiteFilter);
    Assert.assertEquals("wrong", 0, summary.wrong);
    Assert.assertEquals("exceptions", 0, summary.exceptions);
    Assert.assertTrue("at least one test executed",summary.right>0);
  }
}
