package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class PurgeHistoryResponderTest {
  private File resultsDirectory;
  private FitNesseContext context;
  private PurgeHistoryResponder responder;
  private MockRequest request;


  @Before
  public void setup() throws Exception {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();
    responder = new PurgeHistoryResponder();
    Properties properties = new Properties();
    properties.setProperty("test.history.path", resultsDirectory.getPath());
    context = FitNesseUtil.makeTestContext(properties);
    request = new MockRequest();
    request.setResource("TestPage");
  }

  @After
  public void teardown() throws IOException {
    removeResultsDirectory();
  }

  private void removeResultsDirectory() throws IOException {
    if (resultsDirectory.exists())
      FileUtil.deleteFileSystemDirectory(resultsDirectory);
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
    public void deleteTestHistoryOlderThanDays(File resultsDirectory, int days) {
      daysDeleted = days;
    }
  }
}
