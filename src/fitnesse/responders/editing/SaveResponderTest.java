// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.testutil.RegexTest;
import fitnesse.components.SaveRecorder;
import fitnesse.http.*;
import fitnesse.wiki.*;

public class SaveResponderTest extends RegexTest
{
	private WikiPage root;
	private Response response;
	public MockRequest request;
	public Responder responder;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		request = new MockRequest();
		responder = new SaveResponder();
	}

	public void testResponse() throws Exception
	{
		crawler.addPage(root, PathParser.parse("ChildPage"));
		request.setResource("ChildPage");
		request.addInput(EditResponder.SAVE_ID, "12345");
		request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
		request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());


		Response response = responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(303, response.getStatus());
		assertHasRegexp("Location: ChildPage", response.makeHttpHeaders());

		String newContent = root.getChildPage("ChildPage").getData().getContent();
		assertEquals("some new content", newContent);

		checkRecentChanges(root, "ChildPage");
	}

	private void checkRecentChanges(WikiPage source, String changedPage) throws Exception
	{
		assertTrue("RecentChanges should exist", source.hasChildPage("RecentChanges"));
		String recentChanges = source.getChildPage("RecentChanges").getData().getContent();
		assertTrue("ChildPage should be in RecentChanges", recentChanges.indexOf(changedPage) != -1);
	}

	public void testCanCreatePage() throws Exception
	{
		request.setResource("ChildPageTwo");
		request.addInput(EditResponder.SAVE_ID, "12345");
		request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
		request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());



		responder.makeResponse(new FitNesseContext(root), request);

		assertEquals(true, root.hasChildPage("ChildPageTwo"));
		String newContent = root.getChildPage("ChildPageTwo").getData().getContent();
		assertEquals("some new content", newContent);
		assertTrue("RecentChanges should exist", root.hasChildPage("RecentChanges"));
		checkRecentChanges(root, "ChildPageTwo");
	}

	public void testKnowsWhenToMerge() throws Exception
	{
		String simplePageName = "SimplePageName";
		createAndSaveANewPage(simplePageName);

		request.setResource(simplePageName);
		request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
		request.addInput(EditResponder.SAVE_ID, "" + (SaveRecorder.newIdNumber() - 10000));
		request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

		SimpleResponse response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);

		assertHasRegexp("Merge", response.getContent());
	}

	public void testKnowWhenNotToMerge() throws Exception
	{
		String pageName = "NewPage";
		createAndSaveANewPage(pageName);
      String newContent = "some new Content work damn you!";
		request.setResource(pageName);
		request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent);
		request.addInput(EditResponder.SAVE_ID, "" + SaveRecorder.newIdNumber());
		request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

      Response response = responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(303, response.getStatus());

		request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent + " Ok I'm working now");
		request.addInput(EditResponder.SAVE_ID, "" + SaveRecorder.newIdNumber());
		response = responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(303, response.getStatus());
	}

	public void testUsernameIsSavedInPageProperties() throws Exception
	{
		addRequestParameters();
		request.setCredentials("Aladdin", "open sesame");
		response = responder.makeResponse(new FitNesseContext(root), request);

		String user = root.getChildPage("EditPage").getData().getAttribute(WikiPage.LAST_MODIFYING_USER);
		assertEquals("Aladdin", user);
	}

	private void createAndSaveANewPage(String pageName) throws Exception
	{
		WikiPage simplePage = crawler.addPage(root, PathParser.parse(pageName));

		PageData data = simplePage.getData();
		SaveRecorder.pageSaved(data);
		simplePage.commit(data);
	}

	private void doSimpleEdit() throws Exception
	{
		crawler.addPage(root, PathParser.parse("EditPage"));
		addRequestParameters();

		response = responder.makeResponse(new FitNesseContext(root), request);
	}

	private void addRequestParameters()
	{
		request.setResource("EditPage");
		request.addInput(EditResponder.SAVE_ID, "12345");
		request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
		request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());
	}

	public void testHasVersionHeader() throws Exception
	{
		doSimpleEdit();
		assertTrue("header missing", response.getHeader("Previous-Version") != null);
	}
}
