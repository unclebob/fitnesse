package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.*;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

public abstract class BaseFormatter implements ResultsListener {

  protected WikiPage page = null;
  protected FitNesseContext context;
  public static int finalErrorCount = 0;
  protected int testCount = 0;
  protected int failCount = 0;

  public abstract void writeHead(String pageType) throws Exception;

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
  public void errorOccured() {
    try {
      allTestingComplete(new TimeMeasurement().start().stop());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    finalErrorCount = failCount;
  }
  
  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void testComplete(TestPage test, TestSummary summary, TimeMeasurement timeMeasurement) throws Exception {
    testCount++;
    if (summary.wrong > 0) {
      failCount++;
    }
    if (summary.exceptions > 0) {
      failCount++;
    }
  }

  public void addMessageForBlankHtml() throws Exception {
  }

  public int getErrorCount() {
    return 0;
  }
  
}

