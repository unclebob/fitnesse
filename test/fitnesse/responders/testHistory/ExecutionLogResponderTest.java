package fitnesse.responders.testHistory;

import java.io.File;
import java.io.IOException;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class ExecutionLogResponderTest {

  private FitNesseContext context;
  private File resultsDirectory;

  @Before
  public void setUp() throws IOException {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();

    context = FitNesseUtil.makeTestContext();
  }

  @After
  public void teardown() throws IOException {
    removeResultsDirectory();
  }

  private void removeResultsDirectory() throws IOException {
    if (resultsDirectory.exists())
      FileUtil.deleteFileSystemDirectory(resultsDirectory);
  }

  private File addPageDirectory(String pageName) {
    File pageDirectory = new File(resultsDirectory, pageName);
    pageDirectory.mkdir();
    return pageDirectory;
  }

  private File addTestResult(File pageDirectory, String testResultFileName) throws IOException {
    File testResultFile = new File(pageDirectory, testResultFileName + ".xml");
    FileUtil.createFile(testResultFile, "<?xml version=\"1.0\"?>\n" +
            "<testResults></testResults>");
    return testResultFile;
  }

  @Test
  public void provideMessageInAbsenceOfLogs() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");

    ExecutionLogResponder responder = new ExecutionLogResponder();
    responder.setResultsDirectory(resultsDirectory);
    MockRequest request = new MockRequest();
    request.setResource("TestPage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    assertThat(response.getContent(), containsString("No execution log available."));
  }
}
