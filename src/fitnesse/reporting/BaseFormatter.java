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
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseFormatter implements TestSystemListener<WikiTestPage>, Closeable {
  protected final Logger LOG = Logger.getLogger(getClass().getName());

  private final WikiPage page;
  protected final FitNesseContext context;
  // This counter is used by the command line executor and a few tests
  @Deprecated
  public static int finalErrorCount = 0;

  // TODO: testCount and failCount are only used in TestTextFormatter
  @Deprecated
  protected int testCount = 0;
  @Deprecated
  protected int failCount = 0;

  protected BaseFormatter() {
    this.page = null;
    this.context = null;
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
      LOG.log(Level.INFO, "error registered in test system", cause);
    }
  }

  @Override
  public void close() throws IOException {
    finalErrorCount = failCount;
  }


  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testStarted(WikiTestPage testPage) throws IOException {
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
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

