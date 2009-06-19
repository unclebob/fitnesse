package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PurgeHistoryResponderTest {
  private File resultsDirectory;
  private TestHistory history;
  private FitNesseContext context;
  private SimpleResponse response;
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

  private void makeResponse() throws Exception {
     response = (SimpleResponse) responder.makeResponse(context, request);
   }

  @Test
  public void shouldBeAbleToGetTheCorrectMinimumDate() throws Exception {
    long fakeDate = 1245190575759L;
    Date date = new Date(fakeDate);
    long dateStandard = 20090616171615L;
    long formatDays = 1000000;
    dateStandard = dateStandard - 10 * formatDays;
    responder.setTodaysDate(date);
    long resultDate = responder.getTheMinimumDate(10);
    assertEquals(dateStandard, resultDate);
  }

  @Test
  public void shouldBeAbleToDeleteSomeTestHistory() throws Exception {
    responder.setTodaysDate(new Date());
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090402000000_1_0_0_0");
    addTestResult(pageDirectory, "20090602000000_1_0_0_0");
    
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(2, pageHistory.size());
    int days = 30;
    responder.deleteTestHistoryOlderThan(days);
    history.readHistoryDirectory(resultsDirectory);
    pageHistory = history.getPageHistory("SomePage");
    assertEquals(1, pageHistory.size());

    deleteDirectory(pageDirectory);
  }

  private void deleteDirectory(File file) {
    FileUtil.deleteFileSystemDirectory(file);
  }

  @Test
  public void shouldDeleteHistoryFromRequest() throws Exception {
    request.addInput("purgeHistory","");
    request.addInput("days", "30");
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090402000000_1_0_0_0");
    addTestResult(pageDirectory, "20090602000000_1_0_0_0");
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(2, pageHistory.size());

    responder.makeResponse(context,request);
    history.readHistoryDirectory(resultsDirectory);
    pageHistory = history.getPageHistory("SomePage");
    assertEquals(1, pageHistory.size());
  }

  @Test
  public void shouldMakeErrorRequestWhenGetsInvalidNumberOfDays() throws Exception {
    request.addInput("purgeHistory","");
    request.addInput("days","-42");
    history.readHistoryDirectory(resultsDirectory);
    Response response = responder.makeResponse(context,request);
    assertEquals(response.getStatus(),400);
  }
}
