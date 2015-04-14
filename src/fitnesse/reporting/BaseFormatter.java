package fitnesse.reporting;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.logging.Logger;

public abstract class BaseFormatter implements TestSystemListener<WikiTestPage> {
  protected final Logger LOG = Logger.getLogger(getClass().getName());

  private final WikiPage page;

  protected BaseFormatter() {
    this.page = null;
  }

  protected BaseFormatter(final WikiPage page) {
    this.page = page;
  }

  protected WikiPage getPage() {
    return page;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) throws IOException {
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
  public void testSystemStopped(TestSystem testSystem, Throwable cause) {
  }
}

