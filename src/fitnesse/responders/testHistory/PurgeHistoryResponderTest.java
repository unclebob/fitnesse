package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurgeHistoryResponderTest {
  private File resultsDirectory;
  private TestHistory history;
  private FitNesseContext context;
  private PurgeHistoryResponder responder;
  private MockRequest request;


  @Before
  public void setup() throws Exception {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();
    history = new TestHistory();
    responder = new PurgeHistoryResponder();
    responder.setResultsDirectory(resultsDirectory);
    context = new FitNesseContext();
    request = new MockRequest();
    request.setResource("TestPage");
  }

  @After
  public void teardown() {
    removeResultsDirectory();
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
    pageDirectory.mkdir();
    return pageDirectory;
  }

  @Test
  public void shouldBeAbleToSubtractDaysFromDates() throws Exception {
    Date date = makeDate("20090616171615");
    responder.setTodaysDate(date);
    Date resultDate = responder.getDateDaysEarlier(10);
    Date tenDaysEarlier = makeDate("20090606171615");
    assertEquals(tenDaysEarlier, resultDate);
  }

  private Date makeDate(String dateString) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
    Date date = format.parse(dateString);
    return date;
  }

  @Test
  public void shouldBeAbleToDeleteSomeTestHistory() throws Exception {
    responder.setTodaysDate(makeDate("20090616000000"));
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090614000000_1_0_0_0");
    addTestResult(pageDirectory, "20090615000000_1_0_0_0");

    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(2, pageHistory.size());
    responder.deleteTestHistoryOlderThanDays(1);
    history.readHistoryDirectory(resultsDirectory);
    pageHistory = history.getPageHistory("SomePage");
    assertEquals(1, pageHistory.size());
    assertNotNull(pageHistory.get(makeDate("20090615000000")));
    assertNull(pageHistory.get(makeDate("20090614000000")));
  }

  @Test
  public void shouldDeletePageHistoryDirectoryIfEmptiedByPurge() throws Exception {
    responder.setTodaysDate(makeDate("20090616000000"));
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090614000000_1_0_0_0");
    responder.deleteTestHistoryOlderThanDays(1);
    String[] files = resultsDirectory.list();
    assertEquals(0, files.length);
  }

  @Test
  public void shouldDeleteHistoryFromRequestAndRedirect() throws Exception {
    StubbedPurgeHistoryResponder responder = new StubbedPurgeHistoryResponder();
    request.addInput("days", "30");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(30, responder.daysDeleted);
    assertEquals(303, response.getStatus());
    assertEquals("?testHistory", response.getHeader("Location"));
  }

  @Test
  public void shouldMakeErrorResponseWhenGetsInvalidNumberOfDays() throws Exception {
    request.addInput("days", "-42");
    Response response = responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldMakeErrorResponseWhenItGetsInvalidTypeForNumberOfDays() throws Exception {
    request.addInput("days", "bob");
    Response response = responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());

  }

  private static class StubbedPurgeHistoryResponder extends PurgeHistoryResponder {
    public int daysDeleted = -1;

    @Override
    public void deleteTestHistoryOlderThanDays(int days) throws ParseException {
      daysDeleted = days;
    }
  }
}
