// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.http.*;
import fitnesse.wiki.*;
import fitnesse.*;
import fitnesse.testutil.RegexTest;

public class SymbolicLinkResponderTest extends RegexTest
{
	private WikiPage root;
	private WikiPage pageOne;
	private MockRequest request;
	private Responder responder;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		pageOne = root.addChildPage("PageOne");
		root.addChildPage("PageTwo");

		request = new MockRequest();
		request.setResource("PageOne");
		responder = new SymbolicLinkResponder();
	}

	public void testSubmitGoodForm() throws Exception
	{
		request.addInput("linkName", "SymLink");
		request.addInput("linkPath", "PageTwo");
		Response response = responder.makeResponse(new FitNesseContext(root), request);

		checkRedirectToProperties(response);

		WikiPage symLink = pageOne.getChildPage("SymLink");
		assertNotNull(symLink);
		assertEquals(SymbolicPage.class, symLink.getClass());
	}

	public void testRemoval() throws Exception
	{
		PageData data = pageOne.getData();
		WikiPageProperty symLinks = data.getProperties().set("SymbolicLinks");
		symLinks.set("SymLink", "PageTwo");
		pageOne.commit(data);
		assertNotNull(pageOne.getChildPage("SymLink"));

    request.addInput("removal", "SymLink");
		Response response = responder.makeResponse(new FitNesseContext(root), request);
		checkRedirectToProperties(response);

		assertNull(pageOne.getChildPage("SymLink"));
	}

	public void testNoPageAtPath() throws Exception
	{
		request.addInput("linkName", "SymLink");
		request.addInput("linkPath", "NonExistingPage");
		Response response = responder.makeResponse(new FitNesseContext(root), request);

    assertEquals(404, response.getStatus());
		String content = ((SimpleResponse)response).getContent();
		assertSubString("doesn't exist", content);
		assertSubString("Error Occured", content);
	}

	public void testAddFailWhenPageAlreadyHasChild() throws Exception
	{
  	pageOne.addChildPage("SymLink");
		request.addInput("linkName", "SymLink");
		request.addInput("linkPath", "PageTwo");
		Response response = responder.makeResponse(new FitNesseContext(root), request);


    assertEquals(412, response.getStatus());
		String content = ((SimpleResponse)response).getContent();
		assertSubString("already has a child named SymLink", content);
		assertSubString("Error Occured", content);
	}

	private void checkRedirectToProperties(Response response)
	{
		assertEquals(303, response.getStatus());
		assertEquals(response.getHeader("Location"), "PageOne?properties");
	}
}
