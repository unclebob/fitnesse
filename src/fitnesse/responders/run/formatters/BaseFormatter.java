package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.ResultsListener;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;

public abstract class BaseFormatter implements ResultsListener {

  protected WikiPage page = null;
  protected FitNesseContext context;
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

  @Override
  public void errorOccurred(Throwable cause) {
    try {
      allTestingComplete(new TimeMeasurement().start().stop());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    finalErrorCount = failCount;
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary summary, TimeMeasurement timeMeasurement) throws IOException {
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
}

