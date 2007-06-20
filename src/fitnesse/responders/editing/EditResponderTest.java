// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class EditResponderTest extends RegexTest
{
	private WikiPage root;
	private MockRequest request;
	private Responder responder;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("root");
		crawler = root.getPageCrawler();
		request = new MockRequest();
		responder = new EditResponder();
	}

	public void testResponse() throws Exception
	{
		crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
		request.setResource("ChildPage");

		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(200, response.getStatus());

		String body = response.getContent();
		assertSubString("<html>", body);
		assertSubString("<form", body);
		assertSubString("method=\"post\"", body);
		assertSubString("child content with &lt;html&gt;", body);
		assertSubString("name=\"responder\"", body);
		assertSubString("name=\"" + EditResponder.SAVE_ID + "\"", body);
		assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
		assertSubString("type=\"submit\"", body);
	}

	public void testRedirectToRefererEffect() throws Exception
	{
		crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
		request.setResource("ChildPage");
		request.addInput("redirectToReferer", true);
		request.addInput("redirectAction", "boom");
		request.addHeader("Referer", "http://fitnesse.org:8080/SomePage");

		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(200, response.getStatus());

		String body = response.getContent();
		HtmlTag redirectInputTag = HtmlUtil.makeInputTag("hidden", "redirect", "http://fitnesse.org:8080/SomePage?boom");
		assertSubString(redirectInputTag.html(), body);
	}

	public void testPasteFromExcelExists() throws Exception
	{
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		String body = response.getContent();
		assertMatches("SpreadsheetTranslator.js", body);
		assertMatches("spreadsheetSupport.js", body);
	}

	public void testTableWizard() throws Exception
	{
		final String TOP_LEVEL = "LevelOne";
		final String MID_LEVEL = "LevelTwo";
		final String BOTTOM_LEVEL = "LevelThree";

		final String FIXTURE_ONE = "FixtureOne";
		final String FIXTURE_TWO = "FixtureTwo";
		final String FIXTURE_THREE = "FixtureThree";

		buildPageHierarchyWithFixtures(TOP_LEVEL, MID_LEVEL, BOTTOM_LEVEL, FIXTURE_ONE, FIXTURE_TWO, FIXTURE_THREE);
		final String pathName = TOP_LEVEL + PathParser.PATH_SEPARATOR + MID_LEVEL + PathParser.PATH_SEPARATOR + BOTTOM_LEVEL;
		String body = invokeEditResponder(pathName);

		assertTrue(body.indexOf("<select name=\"fixtureTable\"") != -1);
		assertSubString("<option value=\"default\">- Insert Fixture Table -", body);
		assertSubString("<option value=\"" + FIXTURE_ONE + "\">" + FIXTURE_ONE, body);
		assertSubString("<option value=\"" + FIXTURE_TWO + "\">" + FIXTURE_TWO, body);
		assertSubString("<option value=\"" + FIXTURE_THREE + "\">" + FIXTURE_THREE, body);
		assertTrue(body.indexOf("Not.A.Fixture") == -1);
	}

	private String invokeEditResponder(final String pathName) throws Exception
	{
		request.setResource(pathName);

		WikiPagePath path = PathParser.parse(pathName);
		setTestAttributeForPage(crawler.getPage(root, path));
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		return response.getContent();
	}

	private void buildPageHierarchyWithFixtures(final String TOP_LEVEL, final String MID_LEVEL, final String BOTTOM_LEVEL, final String FIXTURE_ONE, final String FIXTURE_TWO, final String FIXTURE_THREE) throws Exception
	{
		WikiPagePath topLevelPath = PathParser.parse(TOP_LEVEL);
		WikiPagePath midLevelPath = PathParser.parse(MID_LEVEL);
		WikiPagePath bottomLevelPath = PathParser.parse(BOTTOM_LEVEL);

		WikiPage topPage = crawler.addPage(root, topLevelPath, "!fixture " + FIXTURE_ONE + "\r\nNot.A.Fixture\r\n!fixture " + FIXTURE_TWO);
		WikiPage level2 = crawler.addPage(topPage, midLevelPath, "!fixture " + FIXTURE_THREE);
		crawler.addPage(level2, bottomLevelPath, "Level three");
	}

	public void testMissingPageDoesNotGetCreated() throws Exception
	{
		request.setResource("MissingPage");
		responder.makeResponse(new FitNesseContext(root), request);
		assertFalse(root.hasChildPage("MissingPage"));
	}

	private void setTestAttributeForPage(WikiPage page) throws Exception
	{
		PageData data = page.getData();
		data.setAttribute("Test", "true");
		page.commit(data);
	}
}
