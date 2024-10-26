package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.assertHasRegexp;

public class HistoryComparerResponderTest {
  public HistoryComparerResponder responder;
  public FitNesseContext context;
  public MockRequest request;
  public HistoryComparer mockedComparer;
  private String firstFilePath = "./TestDir/files/testResults/TestFolder/firstFakeFile"
      .replace('/', File.separatorChar);
  private String secondFilePath = "./TestDir/files/testResults/TestFolder/secondFakeFile"
      .replace('/', File.separatorChar);

  @Before
  public void setup() throws Exception {
    context = FitNesseUtil.makeTestContext();
    firstFilePath = context.getRootPagePath() + "/files/testResults/TestFolder/firstFakeFile"
            .replace('/', File.separatorChar);
    secondFilePath = context.getRootPagePath() + "/files/testResults/TestFolder/secondFakeFile"
            .replace('/', File.separatorChar);

    request = new MockRequest();
    mockedComparer = mock(HistoryComparer.class);

    responder = new HistoryComparerResponder(mockedComparer);
    responder.testing = true;
    mockedComparer.resultContent = new ArrayList<>();
    mockedComparer.resultContent.add("pass");
    when(mockedComparer.getResultContent()).thenReturn(
            mockedComparer.resultContent);
    when(mockedComparer.compare(firstFilePath, secondFilePath)).thenReturn(
        true);
    mockedComparer.firstTableResults = new ArrayList<>();
    mockedComparer.secondTableResults = new ArrayList<>();
    mockedComparer.firstTableResults
        .add("<table><tr><td>This is the content</td></tr></table>");
    mockedComparer.secondTableResults
        .add("<table><tr><td>This is the content</td></tr></table>");

    request.addInput("TestResult_firstFakeFile", "");
    request.addInput("TestResult_secondFakeFile", "");
    request.setResource("TestFolder");
    FileUtil.createFile(context.getRootPagePath() + "/files/testResults/TestFolder/firstFakeFile",
        "firstFile");
    FileUtil.createFile(context.getRootPagePath() + "/files/testResults/TestFolder/secondFakeFile",
        "secondFile");
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
    verify(mockedComparer).compare(firstFilePath, secondFilePath);
  }

  @Test
  public void shouldReturnErrorPageIfCompareFails() throws Exception {
    when(mockedComparer.compare(firstFilePath, secondFilePath)).thenReturn(
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
    verify(mockedComparer, never()).compare(anyString(), anyString());
  }

  @Test
  public void shouldReturnErrorPageIfFilesAreNotInTestHistory() throws Exception {
    request = new MockRequest();
    request.addInput("TestResult_../../../../../../../../../etc/passwd", "");
    request.addInput("TestResult_../../../../../../../../../../etc/passwd", "");
    request.setResource("TestFolder");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    assertEquals(400, response.getStatus());
    assertHasRegexp("Compare Failed because the files were not found.",
        response.getContent());
    verify(mockedComparer, never()).compare(anyString(), anyString());
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
    verify(mockedComparer, never()).compare(anyString(), anyString());
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
    verify(mockedComparer, never()).compare(anyString(), anyString());
  }

  @Test
  public void shouldReturnAResponseWithResultContent() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
        request);
    verify(mockedComparer).getResultContent();
    assertHasRegexp("This is the content", response.getContent());
  }

  @After
  public void tearDown() throws IOException {
    FileUtil.deleteFileSystemDirectory("testRoot");
  }
}
