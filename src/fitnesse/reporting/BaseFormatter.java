package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.WikiPage;

import java.io.Closeable;
import java.io.IOException;

public abstract class BaseFormatter implements TestSystemListener<WikiTestPage>, Closeable {

  protected WikiPage page = null;
  protected FitNesseContext context;
  // Thsi counter is used by the command line executor and a few tests
  @Deprecated
  public static int finalErrorCount = 0;
  protected int testCount = 0;
  protected int failCount = 0;

//  public abstract void writeHead(String pageType) throws Exception;

  protected BaseFormatter() {
  }

  protected BaseFormatter(FitNesseContext context, final WikiPage page) {
    this.page = page;
    this.context = context;
  }

  protected WikiPage getPage() {
    return page;
  }

  public void errorOccurred(Throwable cause) {
    if (cause != null) {
      cause.printStackTrace();
    }
    try {
      close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws IOException {
    finalErrorCount = failCount;
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary summary) throws IOException {
    testCount++;
    if (summary.wrong > 0) {
      failCount++;
    }
    if (summary.exceptions > 0) {
      failCount++;
    }
  }

  public void addMessageForBlankHtml() {
  }

  public int getErrorCount() {
    return 0;
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
    if (cause != null) {
      errorOccurred(cause);
    }
  }

}

