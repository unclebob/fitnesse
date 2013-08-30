package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.WikiTestPage;
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
  
  public String getLockFileName(WikiTestPage test) {
	ReadOnlyPageData data = test.parsedData();
	return context.getTestProgressPath() + "/" + data.getVariable("PAGE_PATH") + "." + data.getVariable("PAGE_NAME");
  }

  public void newTestStarted(WikiTestPage test) {
	FileUtil.createFile(getLockFileName(test), "");
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
	FileUtil.deleteFile(getLockFileName(test));
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    //ignore.
  }
}

