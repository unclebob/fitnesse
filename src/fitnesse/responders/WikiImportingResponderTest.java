// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.wiki.*;
import fitnesse.testutil.*;
import fitnesse.http.*;
import fitnesse.FitNesseContext;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.util.XmlUtil;
import org.w3c.dom.Document;

public class WikiImportingResponderTest extends RegexTest
{
	private WikiImportingResponder responder;
	private String baseUrl;
	private WikiPage localRoot;
	private WikiPage pageOne;
	private WikiPage childPageOne;
	private WikiPage pageTwo;
	private WikiPage remoteRoot;

	public void setUp() throws Exception
	{
		createServerSideRoot();

		responder = new WikiImportingResponder();

		FitNesseUtil.startFitnesse(remoteRoot);
		baseUrl = "http://localhost:" + FitNesseUtil.port + "/";
		responder.remoteHostname = "localhost";
		responder.remotePort = FitNesseUtil.port;
		responder.path = new WikiPagePath();
		ChunkedResponse response = new ChunkedResponse();
		response.readyToSend(new MockResponseSender());
		responder.setResponse(response);

		createLocalRoot();
	}

	private void createLocalRoot() throws Exception
	{
		localRoot = InMemoryPage.makeRoot("RooT2");
		pageOne = localRoot.addChildPage("PageOne");
		childPageOne = pageOne.addChildPage("ChildOne");
		pageTwo = localRoot.addChildPage("PageTwo");
	}

	private WikiPage createServerSideRoot() throws Exception
	{
		remoteRoot = InMemoryPage.makeRoot("RooT");
		PageCrawler crawler = remoteRoot.getPageCrawler();
		crawler.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
		crawler.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
		crawler.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");
		return remoteRoot;
	}

	public void tearDown() throws Exception
	{
		FitNesseUtil.stopFitnesse();
	}

	public void testPageAdded() throws Exception
	{
		responder.pageAdded(pageOne);

		PageData data = pageOne.getData();
		assertEquals("page one", data.getContent());
	}

	public void testChildPageAdded() throws Exception
	{
		responder.pageAdded(pageOne);
		responder.pageAdded(childPageOne);

		PageData data = childPageOne.getData();
		assertEquals("child one", data.getContent());
	}

	public void testExiting() throws Exception
	{
		responder.pageAdded(pageOne);
		responder.pageAdded(childPageOne);
		responder.exitPage();
		responder.exitPage();
		responder.pageAdded(pageTwo);

		PageData data = pageTwo.getData();
		assertEquals("page two", data.getContent());
	}

	public void testGetPageTree() throws Exception
	{
		responder.page = childPageOne;
		responder.xmlizerPageHandler = new MockXmlizerPageHandler();
		Document doc = responder.getPageTree();
		assertNotNull(doc);
		String xml = XmlUtil.xmlAsString(doc);

		assertSubString("PageOne", xml);
		assertSubString("PageTwo", xml);
	}

	public void testActionsOfMakeResponse() throws Exception
	{
		Response response = makeSampleResponse(baseUrl);
		new MockResponseSender(response);

		assertEquals(2, pageTwo.getChildren().size());
		WikiPage importedPageOne = pageTwo.getChildPage("PageOne");
		assertNotNull(importedPageOne);
		assertEquals("page one", importedPageOne.getData().getContent());

		WikiPage importedPageTwo = pageTwo.getChildPage("PageTwo");
		assertNotNull(importedPageTwo);
		assertEquals("page two", importedPageTwo.getData().getContent());

		assertEquals(1, importedPageOne.getChildren().size());
		WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
		assertNotNull(importedChildOne);
		assertEquals("child one", importedChildOne.getData().getContent());
	}

	public void testImportingFromNonRootPageUpdatesPageContent() throws Exception
	{
		PageData data = pageTwo.getData();
		data.setAttribute("WikiImportSource", baseUrl + "PageOne");
		data.setContent("nonsense");
		pageTwo.commit(data);

		Response response = makeSampleResponse("blah");
		new MockResponseSender(response);

		data = pageTwo.getData();
		assertEquals("page one", data.getContent());

		assertFalse(data.hasAttribute("WikiImportRoot"));
	}

	public void testImportPropertiesGetAdded() throws Exception
	{
		Response response = makeSampleResponse(baseUrl);
		new MockResponseSender(response);

		checkProperties(pageTwo, "WikiImportRoot", baseUrl);

		WikiPage importedPageOne = pageTwo.getChildPage("PageOne");
		checkProperties(importedPageOne, "WikiImportSource", baseUrl + "PageOne");

		WikiPage importedPageTwo = pageTwo.getChildPage("PageTwo");
		checkProperties(importedPageTwo, "WikiImportSource", baseUrl + "PageTwo");

		WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
		checkProperties(importedChildOne, "WikiImportSource", baseUrl + "PageOne.ChildOne");
	}

	private void checkProperties(WikiPage page, String id, String value) throws Exception
	{
		WikiPageProperties props = page.getData().getProperties();
		assertTrue(props.has(id));
		assertEquals(value, props.get(id));
	}

	public void testHtmlOfMakeResponse() throws Exception
	{
		Response response = makeSampleResponse(baseUrl);
		String content = new MockResponseSender(response).sentData();

		assertSubString("<html>", content);
		assertSubString("Wiki Import", content);

		assertSubString("href=\"PageTwo\"", content);
		assertSubString("href=\"PageTwo.PageOne\"", content);
		assertSubString("href=\"PageTwo.PageOne.ChildOne\"", content);
		assertSubString("href=\"PageTwo.PageTwo\"", content);
		assertSubString("Import complete.", content);
		assertSubString("3 pages were imported.", content);
	}

	private ChunkedResponse makeSampleResponse(String remoteUrl) throws Exception
	{
		responder.remoteHostname = "blah";
		MockRequest request = makeRequest(remoteUrl);

		return getResponse(request);
	}

	private ChunkedResponse getResponse(MockRequest request) throws Exception
	{
		Response response = responder.makeResponse(new FitNesseContext(localRoot), request);
		assertTrue(response instanceof ChunkedResponse);
		ChunkedResponse chunkedResponse = (ChunkedResponse) response;
		return chunkedResponse;
	}

	private MockRequest makeRequest(String remoteUrl)
	{
		MockRequest request = new MockRequest();
		request.setResource("PageTwo");
		request.addInput("responder", "import");
		request.addInput("remoteUrl", remoteUrl);
		return request;
	}

	public void testMakeResponseImportingNonRootPage() throws Exception
	{
		responder.remoteHostname = "blah";
		MockRequest request = makeRequest(baseUrl + "PageOne");

		Response response = responder.makeResponse(new FitNesseContext(localRoot), request);
		String content = new MockResponseSender(response).sentData();

		assertNotNull(pageTwo.getChildPage("ChildOne"));
		assertSubString("href=\"PageTwo.ChildOne\"", content);
		assertSubString(">ChildOne<", content);
	}

	public void testUrlParsing() throws Exception
	{
		testUrlParsing("http://mysite.com", "mysite.com", 80, "");
		testUrlParsing("http://mysite.com/", "mysite.com", 80, "");
		testUrlParsing("http://mysite.com:8080/", "mysite.com", 8080, "");
		testUrlParsing("http://mysite.com:8080", "mysite.com", 8080, "");
		testUrlParsing("http://mysite.com:80/", "mysite.com", 80, "");
		testUrlParsing("http://mysite.com/PageOne", "mysite.com", 80, "PageOne");
		testUrlParsing("http://mysite.com/PageOne.ChildOne", "mysite.com", 80, "PageOne.ChildOne");
	}

	private void testUrlParsing(String url, String host, int port, String path) throws Exception
	{
		responder.parseUrl(url);
		assertEquals(host, responder.remoteHostname);
		assertEquals(port, responder.remotePort);
		assertEquals(path, PathParser.render(responder.remotePath));
	}

	public void testParsingBadUrl() throws Exception
	{
		try
		{
			responder.parseUrl("blah");
			fail("should have exception");
		}
		catch(Exception e)
		{
			assertEquals("blah is not a valid URL.", e.getMessage());
		}
	}

	public void testParsingUrlWithNonWikiWord() throws Exception
	{
		try
		{
			responder.parseUrl("http://blah.com/notawikiword");
			fail("should throw exception");
		}
		catch(Exception e)
		{
			assertEquals("The URL's resource path, notawikiword, is not a valid WikiWord.", e.getMessage());
		}
	}

	public void testRemoteUrlNotFound() throws Exception
	{
		String remoteUrl = baseUrl + "PageDoesntExist";
		Response response = makeSampleResponse(remoteUrl);

		String content = new MockResponseSender(response).sentData();
		assertSubString("The remote resource, " + remoteUrl + ", was not found.", content);
	}

	public void testBadDomainName() throws Exception
	{
		String remoteUrl = "http://bad.domainthatdoesntexist.com/FrontPage";
		Response response = makeSampleResponse(remoteUrl);

		String content = new MockResponseSender(response).sentData();
		assertSubString("java.net.UnknownHostException: bad.domainthatdoesntexist.com", content);
	}

	public void testErrorMessageForBadUrlProvided() throws Exception
	{
		String remoteUrl = baseUrl + "blah";
		Response response = makeSampleResponse(remoteUrl);

		String content = new MockResponseSender(response).sentData();
		assertSubString("The URL's resource path, blah, is not a valid WikiWord.", content);
	}

	public void testUnauthorizedResponse() throws Exception
	{
		makeSecurePage(remoteRoot);

		Response response = makeSampleResponse(baseUrl);
		String content = new MockResponseSender(response).sentData();

		checkRemoteLoginForm(content);
	}

	private void makeSecurePage(WikiPage page) throws Exception
	{
		PageData data = page.getData();
		data.setAttribute(WikiPage.SECURE_READ);
		page.commit(data);
		FitNesseUtil.context.authenticator = new OneUserAuthenticator("joe", "blow");
	}

	private void checkRemoteLoginForm(String content)
	{
		assertHasRegexp("The wiki at .* requires authentication.", content);
		assertSubString("<form", content);
		assertHasRegexp("<input[^>]*name=\"remoteUsername\"", content);
		assertHasRegexp("<input[^>]*name=\"remotePassword\"", content);
	}

	public void testUnauthorizedResponseFromNonRoot() throws Exception
	{
		WikiPage childPage = remoteRoot.getChildPage("PageOne");
		makeSecurePage(childPage);

		Response response = makeSampleResponse(baseUrl);
		String content = new MockResponseSender(response).sentData();

		assertSubString("The wiki at " + baseUrl + "PageOne requires authentication.", content);
		assertSubString("<form", content);
	}

	public void testImportingFromSecurePageWithCredentials() throws Exception
	{
		makeSecurePage(remoteRoot);

		MockRequest request = makeRequest(baseUrl);
		request.addInput("remoteUsername", "joe");
		request.addInput("remotePassword", "blow");
		Response response = getResponse(request);
		String content = new MockResponseSender(response).sentData();

		assertNotSubString("requires authentication", content);
		assertSubString("3 pages were imported.", content);

		assertEquals("joe", WikiImportingResponder.remoteUsername);
		assertEquals("blow", WikiImportingResponder.remotePassword);
	}
}
