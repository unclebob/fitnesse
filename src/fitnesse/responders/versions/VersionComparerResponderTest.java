package fitnesse.responders.versions;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.assertHasRegexp;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class VersionComparerResponderTest {
  private String firstVersion;
  private String secondVersion;
  private String firstFilePath;
  private String secondFilePath;
  private String currentVersionFilePath;
  private SimpleResponse response;
  private WikiPage root;
  private WikiPage page;
  private VersionComparerResponder responder;
  private MockRequest request;
  private FitNesseContext context;
  private VersionComparer mockedComparer;
  
  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    page = root.getPageCrawler().addPage(root, PathParser.parse("ComparedPage"), "original content");
    PageData data = page.getData();
    
    WikiPageProperties properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "New Page tags");
    data.setContent("new stuff");
    firstVersion = page.commit(data).getName();
    firstFilePath = "./" + FitNesseUtil.base + File.separator + page.getName() + File.separator + firstVersion + ".zip#contents.txt";
    
    data.setContent("even newer stuff");
    secondVersion = page.commit(data).getName();
    secondFilePath = "./" + FitNesseUtil.base + File.separator + page.getName() + File.separator + secondVersion + ".zip#contents.txt";

    currentVersionFilePath = "./" + FitNesseUtil.base + File.separator + page.getName() + File.separator + "contents.txt";
    
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
    when(mockedComparer.compare(firstFilePath, secondFilePath)).thenReturn(true);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
    verify(mockedComparer).compare(firstFilePath, secondFilePath);
  }

  @Test
  public void shouldCompareTheOneVersionSpecifiedToTheCurrentVersion() throws Exception {
    request.addInput("Version_" + secondVersion, "");
    when(mockedComparer.compare(secondFilePath, currentVersionFilePath)).thenReturn(true);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
    verify(mockedComparer).compare(secondFilePath, currentVersionFilePath);
  }
  
  @Test
  public void shouldReturnErrorResponseIfNoVersionsSpecified() throws Exception {
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertHasRegexp(
        "Compare Failed because no Input Files were given. Select one or two please.",
        response.getContent());

  }
}
