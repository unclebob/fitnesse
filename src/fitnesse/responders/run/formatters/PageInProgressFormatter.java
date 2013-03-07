package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.testsystems.TestSummary;
import util.TimeMeasurement;
import util.FileUtil;

import java.io.IOException;

public class PageInProgressFormatter extends NullFormatter {

  public PageInProgressFormatter(FitNesseContext context, final WikiPage page) {
	super(context, page);
  }
  
  public String getLockFileName(TestPage test) {
	ReadOnlyPageData data = test.parsedData();
	return context.getTestProgressPath() + "/" + data.getVariable("PAGE_PATH") + "." + data.getVariable("PAGE_NAME");
  }

  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
	FileUtil.createFile(getLockFileName(test), "");
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
	FileUtil.deleteFile(getLockFileName(test));
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    //ignore.
  }
}

