package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.testsystems.TestSummary;
import util.FileUtil;

import java.io.IOException;

public class PageInProgressFormatter implements TestSystemListener<WikiTestPage> {

  private final FitNesseContext context;

  public PageInProgressFormatter(FitNesseContext context) {
    this.context = context;
  }

  public String getLockFileName(WikiTestPage test) {
    ReadOnlyPageData data = test.getData();
    return context.getTestProgressPath() + "/" + data.getVariable("PAGE_PATH") + "." + data.getVariable("PAGE_NAME");
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
  }

  @Override
  public void testStarted(WikiTestPage test) {
    FileUtil.createFile(getLockFileName(test), "");
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
    FileUtil.deleteFile(getLockFileName(test));
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }
}

