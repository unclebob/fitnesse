// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.responders.ResponderTest;
import fitnesse.wiki.*;

import java.util.List;

public class DeletePageResponderTest extends ResponderTest
{
	final String level1Name = "LevelOne";
	final WikiPagePath level1Path = PathParser.parse(level1Name);
	final String level2Name = "LevelTwo";
	final WikiPagePath level2Path = PathParser.parse(level2Name);
	final WikiPagePath level2FullPath = level1Path.copy().addName(level2Name);
	final String qualifiedLevel2Name = PathParser.render(level2FullPath);

	public void setUp() throws Exception
	{
		super.setUp();
	}

	public void testDeleteConfirmation() throws Exception
	{
		WikiPage level1 = crawler.addPage(root, level1Path);
		crawler.addPage(level1, level2Path);
		MockRequest request = new MockRequest();
		request.setResource(qualifiedLevel2Name);
		request.addInput("deletePage", "");

		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		String content = response.getContent();
		assertSubString("Are you sure you want to delete " + qualifiedLevel2Name, content);
	}

	public void testDeletePage() throws Exception
	{
		WikiPage level1 = crawler.addPage(root, level1Path);
		crawler.addPage(level1, level2Path);
		assertTrue(crawler.pageExists(root, level1Path));
		MockRequest request = new MockRequest();
		request.setResource(level1Name);
		request.addInput("confirmed", "yes");

		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		String page = response.getContent();
		assertNotSubString("Are you sure you want to delete", page);
		assertEquals(303, response.getStatus());
		assertEquals("root", response.getHeader("Location"));
		assertFalse(crawler.pageExists(root, PathParser.parse(level1Name)));

		List children = root.getChildren();
		assertEquals(0, children.size());
	}

	public void testDontDeleteFrontPage() throws Exception
	{
		crawler.addPage(root, PathParser.parse("FrontPage"), "Content");
		request.setResource("FrontPage");
		request.addInput("confirmed", "yes");
		Response response = responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(303, response.getStatus());
		assertEquals("FrontPage", response.getHeader("Location"));
	}

	protected Responder responderInstance()
	{
		return new DeletePageResponder();
	}
}