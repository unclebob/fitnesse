package fitnesse.reporting;

import fitnesse.testsystems.*;
import fitnesse.wiki.WikiPage;

public abstract class BaseFormatter implements Formatter {

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
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testStarted(TestPage testPage) {
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(TestPage test, TestSummary summary) {
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

