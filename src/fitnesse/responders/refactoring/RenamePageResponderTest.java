// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import fitnesse.*;
import fitnesse.responders.ResponderTest;
import fitnesse.wiki.*;
import fitnesse.http.*;

public class RenamePageResponderTest extends ResponderTest
{
	private WikiPagePath pageOnePath;
	private WikiPagePath pageTwoPath;
	private String pageOneName;
	private String pageTwoName;

	protected Responder responderInstance()
	{
		return new RenamePageResponder();
	}

	public void setUp() throws Exception
	{
		super.setUp();
		pageOneName = "PageOne";
		pageTwoName = "PageTwo";
		pageOnePath = PathParser.parse(pageOneName);
		pageTwoPath = PathParser.parse(pageTwoName);
	}

	public void testInvalidName() throws Exception
	{
		String invalidName = "FirstName.SecondName";
		String pageName = "MyPage";
		crawler.addPage(root, PathParser.parse(pageName), "content");
		Response response = doRename(pageName, invalidName);

		assertHasRegexp("Cannot rename", getResponseContent(response));
	}

	public void testDontRenameFrontPage() throws Exception
	{
		String frontPageName = "FrontPage";
		crawler.addPage(root, PathParser.parse(frontPageName), "Content");
		Response response = doRename(frontPageName, "ReNamed");
		assertNotNull(response);
		assertSubString("Cannot rename", getResponseContent(response));
	}

	public void testPageRedirection() throws Exception
	{
		WikiPage pageOne = crawler.addPage(root, PathParser.parse("OneOne"), "Content");
		crawler.addPage(pageOne, PathParser.parse("TwoOne"));
		Response response = doRename("OneOne.TwoOne", "ReName");
		assertNotNull(response);
		assertEquals(303, response.getStatus());
		assertEquals("OneOne.ReName", response.getHeader("Location"));
	}

	public void testPageWasRenamed() throws Exception
	{
		String originalName = "OneOne";
		WikiPagePath originalPath = PathParser.parse(originalName);
		String renamedName = "WonWon";
		WikiPagePath renamedPath = PathParser.parse(renamedName);
		
		crawler.addPage(root, originalPath, "Content");
		assertTrue(crawler.pageExists(root, originalPath));
		assertFalse(crawler.pageExists(root, renamedPath));

		doRename(originalName, renamedName);

		assertTrue(crawler.pageExists(root, renamedPath));
		assertFalse(crawler.pageExists(root, originalPath));
	}

	public void testReferencesChanged() throws Exception
	{
		crawler.addPage(root, pageOnePath, "Line one\nPageTwo\nLine three");
		crawler.addPage(root, pageTwoPath, "Page two content");

		doRename(pageTwoName, "PageThree");
		assertNotNull(root.getChildPage("PageThree"));
		WikiPage pageOne = root.getChildPage(pageOneName);
		assertEquals("Line one\nPageThree\nLine three", pageOne.getData().getContent());
	}

	public void testDontRenameToExistingPage() throws Exception
	{
		crawler.addPage(root, pageOnePath, "Page one content");
		crawler.addPage(root, pageTwoPath, "Page two content");

		Response response = doRename(pageOneName, pageTwoName);
		assertTrue(crawler.pageExists(root, pageOnePath));
		assertTrue(crawler.pageExists(root, pageTwoPath));
		assertEquals("Page two content", root.getChildPage(pageTwoName).getData().getContent());
		assertSubString("Cannot rename", getResponseContent(response));
	}

	public void testChildPagesStayIntactWhenParentIsRenamed() throws Exception
	{
		crawler.addPage(root, pageOnePath, "page one");
		crawler.addPage(root, PathParser.parse("PageOne.ChildPage"), "child page");
		crawler.addPage(root, PathParser.parse("PageOne.ChildPage.GrandChild"), "grand child");

		doRename(pageOneName, pageTwoName);

		WikiPagePath path = PathParser.parse("PageTwo.ChildPage");
		assertTrue(crawler.pageExists(root, path));
		WikiPage page = crawler.getPage(root, path);
		assertNotNull(page);
		assertEquals("child page", page.getData().getContent());

		WikiPagePath grandChildPath = PathParser.parse("PageTwo.ChildPage.GrandChild");
		assertTrue(crawler.pageExists(root, grandChildPath));
		page = crawler.getPage(root, grandChildPath);
		assertNotNull(page);
		assertEquals("grand child", page.getData().getContent());
	}

	private String getResponseContent(Response response) throws Exception
	{
		return new MockResponseSender(response).sentData();
	}

	private Response doRename(String fromName, String toName) throws Exception
	{
		request.setResource(fromName);
		request.addInput("newName", toName);
		return responder.makeResponse(new FitNesseContext(root), request);
	}
}