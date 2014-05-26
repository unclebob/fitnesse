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

public abstract class BaseFormatter implements TestSystemListener<WikiTestPage> {
  protected final Logger LOG = Logger.getLogger(getClass().getName());

  private final WikiPage page;
  protected final FitNesseContext context;

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
      LOG.log(Level.WARNING, "error registered in test system", cause);
    }
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

