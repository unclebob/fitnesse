package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import util.FileUtil;
import static util.RegexTestCase.assertHasRegexp;

import java.util.ArrayList;

public class HistoryComparerResponderTest {
  public HistoryComparerResponder responder;
  public FitNesseContext context;
  public WikiPage root;
  public MockRequest request;
  public HistoryComparer mockedComparer;

  @Before
  public void setup() throws Exception {
    request = new MockRequest();
    mockedComparer = mock(HistoryComparer.class);

    responder = new HistoryComparerResponder(mockedComparer);
    responder.testing = true;
    mockedComparer.resultContent = new ArrayList<String>();
    mockedComparer.resultContent.add("pass");
    when(mockedComparer.getResultContent()).thenReturn(mockedComparer.resultContent);
    when(mockedComparer.compare("testRoot/TestFolder/firstFakeFile", "testRoot/TestFolder/secondFakeFile")).thenReturn(true);
    mockedComparer.firstTableResults = new ArrayList<String>();
    mockedComparer.secondTableResults = new ArrayList<String>();
    mockedComparer.firstTableResults.add("<table><tr><td>This is the content</td></tr></table>");
    mockedComparer.secondTableResults.add("<table><tr><td>This is the content</td></tr></table>");

    request.addInput("TestResult_firstFakeFile", "");
    request.addInput("TestResult_secondFakeFile", "");
    request.setResource("TestFolder");
    FileUtil.createFile("testRoot/TestFolder/firstFakeFile","firstFile");
    FileUtil.createFile("testRoot/TestFolder/secondFakeFile","secondFile");
    responder.baseDir = "testRoot/";
    context = FitNesseUtil.makeTestContext(root);
    root = InMemoryPage.makeRoot("RooT");
  }

  @Test
  public void shouldBeAbleToMakeASimpleResponse() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void shouldGetTwoHistoryFilesFromRequest() throws Exception {
    responder.makeResponse(context, request);
    verify(mockedComparer).compare("testRoot/TestFolder/firstFakeFile", "testRoot/TestFolder/secondFakeFile");
  }

  @Test
  public void shouldReturnErrorPageIfCompareFails() throws Exception {
    when(mockedComparer.compare("testRoot/TestFolder/firstFakeFile", "testRoot/TestFolder/secondFakeFile")).thenReturn(false);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertHasRegexp("These files could not be compared.  They might be suites, or something else might be wrong.", response.getContent());
  }

  @Test
  public void shouldReturnErrorPageIfFilesAreInvalid() throws Exception {
    request = new MockRequest();
    request.addInput("TestResult_firstFile", "");
    request.addInput("TestResult_secondFile", "");
    request.setResource("TestFolder");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertHasRegexp("Compare Failed because the files were not found.", response.getContent());
  }

  @Test
  public void shouldReturnErrorPageIfThereAreTooFewInputFiles() throws Exception {
    request = new MockRequest();
    request.addInput("TestResult_firstFile", "");
    request.setResource("TestFolder");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertHasRegexp("Compare Failed because the wrong number of Input Files were given. Select two please.", response.getContent());
  }

  @Test
  public void shouldReturnErrorPageIfThereAreTooManyInputFiles() throws Exception {
    request.addInput("TestResult_thirdFakeFile", "");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertHasRegexp("Compare Failed because the wrong number of Input Files were given. Select two please.", response.getContent());
  }

  @Test
  public void shouldReturnAResponseWithResultContent() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    verify(mockedComparer).getResultContent();
    assertHasRegexp("This is the content", response.getContent());
  }

  @After
  public void tearDown() {
    FileUtil.deleteFileSystemDirectory("testRoot");
  }
}
