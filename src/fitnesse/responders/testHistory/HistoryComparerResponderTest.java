package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.slimTables.HtmlTableScanner;
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
    mockedComparer.firstScanner = new HtmlTableScanner("<table>x</table>");
    mockedComparer.secondScanner = new HtmlTableScanner("<table>y</table>");
    responder = new HistoryComparerResponder(mockedComparer);
    request.addInput("TestResult_firstFakeFile", "");
    request.addInput("TestResult_secondFakeFile", "");
    request.setResource("TestFolder");
    when(mockedComparer.compare("testRoot/TestFolder/firstFakeFile", "testRoot/TestFolder/secondFakeFile")).thenReturn(true);
    when(mockedComparer.getResultContent()).thenReturn(new String[] {});
    FileUtil.createFile("testRoot/TestFolder/firstFakeFile", "first");
    FileUtil.createFile("testRoot/TestFolder/secondFakeFile", "second");
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
    when(mockedComparer.getResultContent()).thenReturn(new String[]{"This is the Content"});
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    verify(mockedComparer).getResultContent();
    assertHasRegexp("This is the Content", response.getContent());
  }

  @After
  public void tearDown() {
    FileUtil.deleteFileSystemDirectory("testRoot");
  }
}
