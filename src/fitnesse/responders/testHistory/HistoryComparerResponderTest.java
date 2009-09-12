package fitnesse.responders.testHistory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.assertHasRegexp;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;

public class HistoryComparerResponderTest {
  public HistoryComparerResponder responder;
  public FitNesseContext context;
  public WikiPage root;
  public MockRequest request;
  public HistoryComparer mockedComparer;
  private final String FIRST_FILE_PATH = "./TestDir/files/testResults/TestFolder/firstFakeFile"
      .replace('/', File.separatorChar);
  private final String SECOND_FILE_PATH = "./TestDir/files/testResults/TestFolder/secondFakeFile"
      .replace('/', File.separatorChar);

  @Before
  public void setup() throws Exception {
    request = new MockRequest();
    mockedComparer = mock(HistoryComparer.class);

    responder = new HistoryComparerResponder(mockedComparer);
    responder.testing = true;
    mockedComparer.resultContent = new ArrayList<String>();
    mockedComparer.resultContent.add("pass");
    when(mockedComparer.getResultContent()).thenReturn(
        mockedComparer.resultContent);
    when(mockedComparer.compare(FIRST_FILE_PATH, SECOND_FILE_PATH)).thenReturn(
        true);
    mockedComparer.firstTableResults = new ArrayList<String>();
    mockedComparer.secondTableResults = new ArrayList<String>();
    mockedComparer.firstTableResults
        .add("<table><tr><td>This is the content</td></tr></table>");
    mockedComparer.secondTableResults
        .add("<table><tr><td>This is the content</td></tr></table>");

    request.addInput("TestResult_firstFakeFile", "");
    request.addInput("TestResult_secondFakeFile", "");
    request.setResource("TestFolder");
    FileUtil.createFile("TestDir/files/testResults/TestFolder/firstFakeFile",
        "firstFile");
    FileUtil.createFile("TestDir/files/testResults/TestFolder/secondFakeFile",
        "secondFile");
    context = FitNesseUtil.makeTestContext(root);
    root = InMemoryPage.makeRoot("RooT");
  }

  @Test
  public void shouldBeAbleToMakeASimpleResponse() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void shouldGetTwoHistoryFilesFromRequest() throws Exception {
    responder.makeResponse(context, request);
    verify(mockedComparer).compare(FIRST_FILE_PATH, SECOND_FILE_PATH);
  }

  @Test
  public void shouldReturnErrorPageIfCompareFails() throws Exception {
    when(mockedComparer.compare(FIRST_FILE_PATH, SECOND_FILE_PATH)).thenReturn(
        false);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    assertEquals(400, response.getStatus());
    assertHasRegexp(
        "These files could not be compared.  They might be suites, or something else might be wrong.",
        response.getContent());
  }

  @Test
  public void shouldReturnErrorPageIfFilesAreInvalid() throws Exception {
    request = new MockRequest();
    request.addInput("TestResult_firstFile", "");
    request.addInput("TestResult_secondFile", "");
    request.setResource("TestFolder");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    assertEquals(400, response.getStatus());
    assertHasRegexp("Compare Failed because the files were not found.",
        response.getContent());
  }

  @Test
  public void shouldReturnErrorPageIfThereAreTooFewInputFiles()
      throws Exception {
    request = new MockRequest();
    request.addInput("TestResult_firstFile", "");
    request.setResource("TestFolder");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    assertEquals(400, response.getStatus());
    assertHasRegexp(
        "Compare Failed because the wrong number of Input Files were given. Select two please.",
        response.getContent());
  }

  @Test
  public void shouldReturnErrorPageIfThereAreTooManyInputFiles()
      throws Exception {
    request.addInput("TestResult_thirdFakeFile", "");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    assertEquals(400, response.getStatus());
    assertHasRegexp(
        "Compare Failed because the wrong number of Input Files were given. Select two please.",
        response.getContent());
  }

  @Test
  public void shouldReturnAResponseWithResultContent() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    verify(mockedComparer).getResultContent();
    assertHasRegexp("This is the content", response.getContent());
  }

  @After
  public void tearDown() {
    FileUtil.deleteFileSystemDirectory("testRoot");
  }
}
