package fitnesse.responders;

import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockChunkedDataProvider;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static util.RegexTestCase.*;

public class WikiImportingResponderAuthenticationTest {
  private WikiImportingResponder responder;
  private String baseUrl;
  private WikiImporterTest testData;

  @Before
  public void setUp() throws Exception {
    testData = new WikiImporterTest();
    testData.createRemoteRoot(new OneUserAuthenticator("joe", "blow"));
    testData.createLocalRoot();

    FitNesseUtil.startFitnesseWithContext(testData.remoteContext);
    baseUrl = FitNesseUtil.URL;

    createResponder();
  }

  private void createResponder() throws Exception {
    WikiImporter importer = new WikiImporter();
    importer.setDeleteOrphanOption(false);
    responder = new WikiImportingResponder(importer);
    responder.path = new WikiPagePath();
    ChunkedResponse response = new ChunkedResponse("html", new MockChunkedDataProvider());
    response.sendTo(new MockResponseSender());
    responder.setResponse(response);
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  private void makeSecurePage(WikiPage page) throws Exception {
    PageData data = page.getData();
    data.setAttribute(WikiPageProperty.SECURE_READ);
    page.commit(data);
  }

  private void checkRemoteLoginForm(String content) {
    assertHasRegexp("The wiki at .* requires authentication.", content);
    assertSubString("<form", content);
    assertHasRegexp("<input[^>]*name=\"remoteUsername\"", content);
    assertHasRegexp("<input[^>]*name=\"remotePassword\"", content);
  }

  private MockRequest makeRequest(String remoteUrl) {
    MockRequest request = new MockRequest();
    request.setResource("PageTwo");
    request.addInput("responder", "import");
    request.addInput("remoteUrl", remoteUrl);
    return request;
  }

  private ChunkedResponse makeSampleResponse(String remoteUrl) throws Exception {
    MockRequest request = makeRequest(remoteUrl);

    return getResponse(request);
  }

  private ChunkedResponse getResponse(MockRequest request) throws Exception {
    ChunkedResponse response = (ChunkedResponse) responder.makeResponse(testData.localContext, request);
    response.turnOffChunking();
    return response;
  }

  @Test
  public void testUnauthorizedResponse() throws Exception {
    makeSecurePage(testData.remoteContext.getRootPage());

    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    checkRemoteLoginForm(content);
  }

  @Test
  public void testUnauthorizedResponseFromNonRoot() throws Exception {
    WikiPage childPage = testData.remoteContext.getRootPage().getChildPage("PageOne");
    makeSecurePage(childPage);

    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The wiki at " + baseUrl + "PageOne requires authentication.", content);
    assertSubString("<form", content);
  }

  @Test
  public void testImportingFromSecurePageWithCredentials() throws Exception {
    makeSecurePage(testData.remoteContext.getRootPage());

    MockRequest request = makeRequest(baseUrl);
    request.addInput("remoteUsername", "joe");
    request.addInput("remotePassword", "blow");
    Response response = getResponse(request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertNotSubString("requires authentication", content);
    assertSubString("3 pages were imported.", content);
  }

}
