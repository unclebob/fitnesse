package fitnesse.responders.versions;

import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.assertHasRegexp;

public class VersionComparerResponderTest {
  private String firstVersion;
  private String secondVersion;
  private SimpleResponse response;
  private VersionComparerResponder responder;
  private MockRequest request;
  private FitNesseContext context;
  private VersionComparer mockedComparer;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    WikiPage page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("ComparedPage"), "original content");
    PageData data = page.getData();
    firstVersion = page.commit(data).getName();

    WikiPageProperty properties = data.getProperties();
    properties.set(WikiPageProperty.SUITES, "New Page tags");
    data.setContent("new stuff");
    secondVersion = page.commit(data).getName();

    data.setContent("even newer stuff");
    page.commit(data);

    request = new MockRequest();
    request.setResource("ComparedPage");

    mockedComparer = mock(VersionComparer.class);
    responder = new VersionComparerResponder(mockedComparer);
    responder.testing = true;
  }

  @Test
  public void shouldCompareTheTwoVersionsSpecified() throws Exception {
    request.addInput("Version_" + firstVersion, "");
    request.addInput("Version_" + secondVersion, "");
    when(mockedComparer.compare(firstVersion, "original content", secondVersion, "new stuff")).thenReturn(true);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
    verify(mockedComparer).compare(firstVersion, "original content", secondVersion, "new stuff");
  }

  @Test
  public void shouldCompareTheOneVersionSpecifiedToTheCurrentVersion() throws Exception {
    request.addInput("Version_" + secondVersion, "");
    when(mockedComparer.compare(secondVersion, "new stuff", "latest", "even newer stuff")).thenReturn(true);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
    verify(mockedComparer).compare(secondVersion, "new stuff", "latest", "even newer stuff");
  }

  @Test
  public void shouldReturnErrorResponseIfNoVersionsSpecified() throws Exception {
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertHasRegexp(
        "Compare failed because no input files were given. Select one or two please.",
        response.getContent());

  }
}
