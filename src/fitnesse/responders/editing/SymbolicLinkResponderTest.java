// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.util.FileUtil;
import fitnesse.wiki.*;

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

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory("testDir");
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
		WikiPageProperty symLinks = data.getProperties().set(SymbolicPage.PROPERTY_NAME);
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
		String content = ((SimpleResponse) response).getContent();
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
		String content = ((SimpleResponse) response).getContent();
		assertSubString("already has a child named SymLink", content);
		assertSubString("Error Occured", content);
	}

	public void testSubmitFormForLinkToExternalRoot() throws Exception
	{
		FileUtil.createDir("testDir");
		FileUtil.createDir("testDir/ExternalRoot");

		request.addInput("linkName", "SymLink");
		request.addInput("linkPath", "file://testDir/ExternalRoot");
		Response response = responder.makeResponse(new FitNesseContext(root), request);

		checkRedirectToProperties(response);

		WikiPage symLink = pageOne.getChildPage("SymLink");
		assertNotNull(symLink);
		assertEquals(SymbolicPage.class, symLink.getClass());

		WikiPage realPage = ((SymbolicPage) symLink).getRealPage();
		assertEquals(FileSystemPage.class, realPage.getClass());
		assertEquals("testDir/ExternalRoot", ((FileSystemPage) realPage).getFileSystemPath());
	}

	public void testSubmitFormForLinkToExternalRootThatsMissing() throws Exception
	{
		request.addInput("linkName", "SymLink");
		request.addInput("linkPath", "file://testDir/ExternalRoot");
		Response response = responder.makeResponse(new FitNesseContext(root), request);

		assertEquals(404, response.getStatus());
		String content = ((SimpleResponse) response).getContent();
		assertSubString("Cannot create link to the file system path, <b>file://testDir/ExternalRoot</b>.", content);
		assertSubString("Error Occured", content);
	}

	private void checkRedirectToProperties(Response response)
	{
		assertEquals(303, response.getStatus());
		assertEquals(response.getHeader("Location"), "PageOne?properties");
	}
}
