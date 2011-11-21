package fitnesse.responders.run.formatters;

import fitnesse.responders.run.TestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.responders.run.TestSummary;
import util.TimeMeasurement;
import util.FileUtil;

public class PageInProgressFormatter extends NullFormatter {

  public PageInProgressFormatter(final WikiPage page) {
	super();
	this.page = page;
  }
  
  public String getLockFileName(TestPage test) throws Exception {
	PageData data = test.getData();
	return "FitNesseRoot/files/testProgress/" + data.getVariable("PAGE_PATH") + "." + data.getVariable("PAGE_NAME");
  }
    
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws Exception {
	FileUtil.createFile(getLockFileName(test), "");
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
	FileUtil.deleteFile(getLockFileName(test));
  }
}

