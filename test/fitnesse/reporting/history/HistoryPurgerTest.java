package fitnesse.reporting.history;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.wiki.PathParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import static org.junit.Assert.*;

public class HistoryPurgerTest {

  private File resultsDirectory;
  private HistoryPurger historyPurger;

  @Before
  public void setUp() throws ParseException {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();

    new DateAlteringClock(makeDate("20090616000000")).freeze();
    historyPurger = new HistoryPurger(resultsDirectory, 1);
  }

  @After
  public void resetClock() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void shouldBeAbleToSubtractDaysFromDates() throws Exception {
    Date date = makeDate("20090616171615");
    new DateAlteringClock(date).freeze();
    Date resultDate = historyPurger.getDateDaysAgo(10);
    Date tenDaysEarlier = makeDate("20090606171615");
    assertEquals(tenDaysEarlier, resultDate);
  }

  @Test
  public void shouldBeAbleToDeleteSomeTestHistory() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090614000000_1_0_0_0");
    addTestResult(pageDirectory, "20090615000000_1_0_0_0");

    historyPurger.deleteTestHistoryOlderThanDays();

    TestHistory history = new TestHistory();
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(1, pageHistory.size());
    assertNotNull(pageHistory.get(makeDate("20090615000000")));
    assertNull(pageHistory.get(makeDate("20090614000000")));
  }

  @Test
  public void shouldBeAbleToDeletePagesFromASuite() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090614000000_1_0_0_0");
    addTestResult(pageDirectory, "20090615000000_1_0_0_0");

    File subPageDirectory = addPageDirectory("SomePage.SubPage");
    addTestResult(subPageDirectory, "20090614000000_1_0_0_0");
    addTestResult(subPageDirectory, "20090615000000_1_0_0_0");

    File otherPageDirectory = addPageDirectory("OtherPage");
    addTestResult(otherPageDirectory, "20090614000000_1_0_0_0");
    addTestResult(otherPageDirectory, "20090615000000_1_0_0_0");

    historyPurger.deleteTestHistoryOlderThanDays(PathParser.parse("SomePage"));

    TestHistory history = new TestHistory();
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertNotNull(pageHistory.get(makeDate("20090615000000")));
    assertNull(pageHistory.get(makeDate("20090614000000")));

    pageHistory = history.getPageHistory("SomePage.SubPage");
    assertEquals(1, pageHistory.size());
    assertNotNull(pageHistory.get(makeDate("20090615000000")));
    assertNull(pageHistory.get(makeDate("20090614000000")));

    pageHistory = history.getPageHistory("OtherPage");
    assertEquals(2, pageHistory.size());
    assertNotNull(pageHistory.get(makeDate("20090615000000")));
    assertNotNull(pageHistory.get(makeDate("20090614000000")));
  }

  @Test
  public void shouldDeletePageHistoryDirectoryIfEmptiedByPurge() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090614000000_1_0_0_0");
    
    historyPurger.deleteTestHistoryOlderThanDays();
    
    String[] files = resultsDirectory.list();
    assertEquals(0, files.length);
  }

  @Test
  public void fileWithInvalidDateWillNotBeRemoved() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "someFile");
    
    historyPurger.deleteTestHistoryOlderThanDays();
    
    String[] files = new File(resultsDirectory, "SomePage").list();
    assertEquals(1, files.length);
    assertEquals("someFile.xml", files[0]);
  }

  private void removeResultsDirectory() {
    if (resultsDirectory.exists())
      FileUtil.deleteFileSystemDirectory(resultsDirectory);
  }

  private File addTestResult(File pageDirectory, String testResultFileName) throws IOException {
    File testResultFile = new File(pageDirectory, testResultFileName + ".xml");
    testResultFile.createNewFile();
    return testResultFile;
  }

  private File addPageDirectory(String pageName) {
    File pageDirectory = new File(resultsDirectory, pageName);
    pageDirectory.mkdirs();
    return pageDirectory;
  }

  private Date makeDate(String dateString) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
    Date date = format.parse(dateString);
    return date;
  }

}