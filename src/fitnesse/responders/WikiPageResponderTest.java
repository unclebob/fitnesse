// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.authentication.*;
import fitnesse.http.*;
import fitnesse.testutil.*;
import fitnesse.wiki.*;

public class WikiPageResponderTest extends RegexTest
{
	private WikiPage root;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("root");
		crawler = root.getPageCrawler();
	}

	public void testResponse() throws Exception
	{
		crawler.addPage(root, PathParser.parse("ChildPage"), "child content");
		MockRequest request = new MockRequest();
		request.setResource("ChildPage");

		Responder responder = new WikiPageResponder();
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

		assertEquals(200, response.getStatus());

		String body = response.getContent();

		assertSubString("<html>", body);
		assertSubString("<body", body);
		assertSubString("child content", body);
		assertSubString("href=\"ChildPage?whereUsed\"", body);
		assertSubString("ChildPage</span>", body);
		assertSubString("Cache-Control: max-age=0", response.makeHttpHeaders());
	}

	public void testAttributeButtons() throws Exception
	{
		crawler.addPage(root, PathParser.parse("NormalPage"));
		WikiPage noButtonsPage = crawler.addPage(root, PathParser.parse("NoButtonPage"));
		for(int i = 0; i < WikiPage.NON_SECURITY_ATTRIBUTES.length; i++)
		{
			String attribute = WikiPage.NON_SECURITY_ATTRIBUTES[i];
			PageData data = noButtonsPage.getData();
			data.removeAttribute(attribute);
			noButtonsPage.commit(data);
		}

		SimpleResponse response = requestPage("NormalPage");
		assertSubString("<!--Edit button-->", response.getContent());
		assertSubString("<!--Search button-->", response.getContent());
		assertSubString("<!--Versions button-->", response.getContent());
		assertNotSubString("<!--Suite button-->", response.getContent());
		assertNotSubString("<!--Test button-->", response.getContent());

		response = requestPage("NoButtonPage");
		assertNotSubString("<!--Edit button-->", response.getContent());
		assertNotSubString("<!--Search button-->", response.getContent());
		assertNotSubString("<!--Versions button-->", response.getContent());
		assertNotSubString("<!--Suite button-->", response.getContent());
		assertNotSubString("<!--Test button-->", response.getContent());
	}

	public void testHeadersAndFooters() throws Exception
	{
		crawler.addPage(root, PathParser.parse("NormalPage"), "normal");
		crawler.addPage(root, PathParser.parse("TestPage"), "test page");
		crawler.addPage(root, PathParser.parse("PageHeader"), "header");
		crawler.addPage(root, PathParser.parse("PageFooter"), "footer");
		crawler.addPage(root, PathParser.parse("SetUp"), "setup");
		crawler.addPage(root, PathParser.parse("TearDown"), "teardown");

		SimpleResponse response = requestPage("NormalPage");
		String content = response.getContent();
		assertHasRegexp("header", content);
		assertHasRegexp("normal", content);
		assertHasRegexp("footer", content);
		assertDoesntHaveRegexp("setup", content);
		assertDoesntHaveRegexp("teardown", content);

		response = requestPage("TestPage");
		content = response.getContent();
		assertHasRegexp("header", content);
		assertHasRegexp("test page", content);
		assertHasRegexp("footer", content);
		assertHasRegexp("setup", content);
		assertHasRegexp("teardown", content);
	}

	private SimpleResponse requestPage(String name) throws Exception
	{
		MockRequest request = new MockRequest();
		request.setResource(name);
		Responder responder = new WikiPageResponder();
		return (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
	}

	public void testShouldGetVirtualPage() throws Exception
	{
		WikiPage pageOne = crawler.addPage(root, PathParser.parse("TargetPage"), "some content");
		crawler.addPage(pageOne, PathParser.parse("ChildPage"), "child content");
		WikiPage linkerPage = crawler.addPage(root, PathParser.parse("LinkerPage"), "linker content");
		FitNesseUtil.bindVirtualLinkToPage(linkerPage, pageOne);
		SimpleResponse response = requestPage("LinkerPage.ChildPage");

		assertSubString("child content", response.getContent());
	}

	public void testVirtualPageIndication() throws Exception
	{
		WikiPage targetPage = crawler.addPage(root, PathParser.parse("TargetPage"));
		crawler.addPage(targetPage, PathParser.parse("ChildPage"));
		WikiPage linkPage = (BaseWikiPage) crawler.addPage(root, PathParser.parse("LinkPage"));
		VirtualCouplingExtensionTest.setVirtualWiki(linkPage, "http://localhost:" + FitNesseUtil.port + "/TargetPage");

		FitNesseUtil.startFitnesse(root);
		SimpleResponse response = null;
		try
		{
			response = requestPage("LinkPage.ChildPage");
		}
		finally
		{
			FitNesseUtil.stopFitnesse();
		}

		assertSubString("<body class=\"virtual\">", response.getContent());
	}

	public void testImportedPageIndication() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("SamplePage"));
		PageData data = page.getData();
		WikiImportProperty importProperty = new WikiImportProperty("blah");
		importProperty.addTo(data.getProperties());
		page.commit(data);

		String content = requestPage("SamplePage").getContent();

		assertSubString("<body class=\"imported\">", content);
	}

	public void testResponderIsSecureReadOperation() throws Exception
	{
		Responder responder = new WikiPageResponder();
		assertTrue(responder instanceof SecureResponder);
		SecureOperation operation = ((SecureResponder) responder).getSecureOperation();
		assertEquals(SecureReadOperation.class, operation.getClass());
	}

}
