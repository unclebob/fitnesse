// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;

public class DirectoryResponderTest extends RegexTest
{
	MockRequest request;
	private SimpleResponse response;
	private FitNesseContext context;

	public void setUp() throws Exception
	{
		request = new MockRequest();
		context = new FitNesseContext();
		context.rootPagePath = SampleFileUtility.base;
		SampleFileUtility.makeSampleFiles();
	}

	public void tearDown() throws Exception
	{
		SampleFileUtility.deleteSampleFiles();
	}

	public void testDirectotyListing() throws Exception
	{
		request.setResource("files/testDir/");
		Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
		response = (SimpleResponse) responder.makeResponse(context, request);
		assertHasRegexp("testDir", response.getContent());
		assertHasRegexp("testFile2</a>", response.getContent());
		assertHasRegexp("testFile3</a>", response.getContent());
		assertHasRegexp("<a href=\"/", response.getContent());
	}

	public void testButtons() throws Exception
	{
		request.setResource("files/testDir/");
		Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
		response = (SimpleResponse) responder.makeResponse(context, request);

		assertHasRegexp("upload form", response.getContent());
		assertHasRegexp("create directory form", response.getContent());
	}

	public void testHtml() throws Exception
	{
		request.setResource("files/testDir/");
		Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
		response = (SimpleResponse) responder.makeResponse(context, request);
		assertHasRegexp("/files/", response.getContent());
	}

	public void testRedirectForDirectory() throws Exception
	{
		request.setResource("files/testDir");
		Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
		Response response = responder.makeResponse(context, request);
		assertEquals(303, response.getStatus());
		assertEquals("/files/testDir/", response.getHeader("Location"));
	}

	public void testFrontPageSidebarButtonPresent() throws Exception
	{
		request.setResource("files/testDir/");
		Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
		response = (SimpleResponse) responder.makeResponse(context, request);

		assertHasRegexp("<div class=\"sidebar\">", response.getContent());
		assertHasRegexp("<div class=\"actions\">", response.getContent());
		assertHasRegexp("<a href=\"/FrontPage\" accesskey=\"f\">FrontPage</a>", response.getContent());
	}

	public void testSizeString() throws Exception
	{
		assertEquals("", DirectoryResponder.getSizeString(SampleFileUtility.testDir));
		assertEquals("13 bytes", DirectoryResponder.getSizeString(SampleFileUtility.testFile1));
	}
}
