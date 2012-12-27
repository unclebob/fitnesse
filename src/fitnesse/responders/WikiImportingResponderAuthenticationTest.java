package fitnesse.responders;

import fitnesse.FitNesseContext;
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
import util.RegexTestCase;

public class WikiImportingResponderAuthenticationTest extends RegexTestCase {
  private WikiImportingResponder responder;
  private String baseUrl;
  private WikiImporterTest testData;

  public void setUp() throws Exception {
    testData = new WikiImporterTest();
    testData.createRemoteRoot();
    testData.createLocalRoot();

    FitNesseContext context = FitNesseUtil.makeTestContext(testData.remoteRoot, new OneUserAuthenticator("joe", "blow"));

    FitNesseUtil.startFitnesseWithContext(context);
    baseUrl = FitNesseUtil.URL;

    createResponder();
  }

  private void createResponder() throws Exception {
    responder = new WikiImportingResponder();
    responder.path = new WikiPagePath();
    ChunkedResponse response = new ChunkedResponse("html", new MockChunkedDataProvider());
    response.sendTo(new MockResponseSender());
    responder.setResponse(response);
    responder.getImporter().setDeleteOrphanOption(false);
  }

  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  private void makeSecurePage(WikiPage page) throws Exception {
    PageData data = page.getData();
    data.setAttribute(PageData.PropertySECURE_READ);
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

  private ChunkedResponse makeSampleResponse(String remoteUrl) {
    MockRequest request = makeRequest(remoteUrl);

    return getResponse(request);
  }

  private ChunkedResponse getResponse(MockRequest request) {
    ChunkedResponse response = (ChunkedResponse) responder.makeResponse(FitNesseUtil.makeTestContext(testData.localRoot), request);
    response.turnOffChunking();
    return response;
  }

  public void testUnauthorizedResponse() throws Exception {
    makeSecurePage(testData.remoteRoot);

    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    checkRemoteLoginForm(content);
  }

  public void testUnauthorizedResponseFromNonRoot() throws Exception {
    WikiPage childPage = testData.remoteRoot.getChildPage("PageOne");
    makeSecurePage(childPage);

    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The wiki at " + baseUrl + "PageOne requires authentication.", content);
    assertSubString("<form", content);
  }

  public void testImportingFromSecurePageWithCredentials() throws Exception {
    makeSecurePage(testData.remoteRoot);

    MockRequest request = makeRequest(baseUrl);
    request.addInput("remoteUsername", "joe");
    request.addInput("remotePassword", "blow");
    Response response = getResponse(request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertNotSubString("requires authentication", content);
    assertSubString("3 pages were imported.", content);

    assertEquals("joe", WikiImporter.remoteUsername);
    assertEquals("blow", WikiImporter.remotePassword);
  }

}
