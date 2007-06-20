// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class FitClientResponderTest extends RegexTest
{
	private WikiPage root;
	private FitClientResponder responder;
	private MockRequest request;
	private FitNesseContext context;
	private Response response;
	private MockResponseSender sender;
	private static PageCrawler crawler;
	private static WikiPage suite;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		responder = new FitClientResponder();
		request = new MockRequest();
		context = new FitNesseContext(root);

		buildSuite(root);
	}

	public static void buildSuite(WikiPage root) throws Exception
	{
		crawler = root.getPageCrawler();
		suite = crawler.addPage(root, PathParser.parse("SuitePage"), "!path classes\n");
		WikiPage page1 = crawler.addPage(suite, PathParser.parse("TestPassing"), "!|fitnesse.testutil.PassFixture|\n");
		WikiPage page2 = crawler.addPage(suite, PathParser.parse("TestFailing"), "!|fitnesse.testutil.FailFixture|\n");
		crawler.addPage(suite, PathParser.parse("TestError"), "!|fitnesse.testutil.ErrorFixture|\n");
		crawler.addPage(suite, PathParser.parse("TestIgnore"), "!|fitnesse.testutil.IgnoreFixture|\n");
		crawler.addPage(suite, PathParser.parse("SomePage"), "This is just some page.");

		PageData data1 = page1.getData();
		PageData data2 = page2.getData();
		data1.setAttribute(PropertiesResponder.SUITES, "foo");
		data2.setAttribute(PropertiesResponder.SUITES, "bar, smoke");
		page1.commit(data1);
		page2.commit(data2);
	}

	public void tearDown() throws Exception
	{
	}

	public void testPageNotFound() throws Exception
	{
		String result = getResultFor("MissingPage");
		assertSubString("MissingPage was not found", result);
	}

	public void testOneTest() throws Exception
	{
		String result = getResultFor("SuitePage.TestPassing");
		assertEquals("0000000000", result.substring(0, 10));
		assertSubString("PassFixture", result);
	}

	public void testSuite() throws Exception
	{
		String result = getResultFor("SuitePage");
		assertEquals("0000000000", result.substring(0, 10));
		assertSubString("PassFixture", result);
		assertSubString("FailFixture", result);
		assertSubString("ErrorFixture", result);
		assertSubString("IgnoreFixture", result);
		assertNotSubString("some page", result);
	}

	public void testRelativePageNamesIncluded() throws Exception
	{
		String result = getResultFor("SuitePage");
		assertNotSubString("SuitePage", result);
		assertSubString("TestPassing", result);
		assertSubString("TestFailing", result);
		assertSubString("TestError", result);
		assertSubString("TestIgnore", result);
	}

	public void testPageThatIsNoATest() throws Exception
	{
		String result = getResultFor("SuitePage.SomePage");
		assertSubString("SomePage is neither a Test page nor a Suite page.", result);
	}

	private String getResultFor(String name) throws Exception
	{
		return getResultFor(name, false);
	}

	private String getResultFor(String name, boolean addPaths) throws Exception
	{
		request.setResource(name);
		if(addPaths)
			request.addInput("includePaths", "blah");
		response = responder.makeResponse(context, request);
		sender = new MockResponseSender(response);
		String result = sender.sentData();
		return result;
	}

	public void testWithClasspathOnSuite() throws Exception
	{
		String result = getResultFor("SuitePage", true);
		assertTrue(result.startsWith("00000000000000000007classes"));
	}

	public void testWithClasspathOnTestInSuite() throws Exception
	{
		crawler.addPage(suite, PathParser.parse("TestPage"), "!path jar.jar\n!path /some/dir/with/.class/files\n!|fitnesse.testutil.IgnoreFixture|\n");
		String result = getResultFor("SuitePage.TestPage", true);

		assertSubString("classes", result);
		assertSubString("jar.jar", result);
		assertSubString("/some/dir/with/.class/files", result);
	}
}
