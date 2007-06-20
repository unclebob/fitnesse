// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.versions;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class VersionResponderTest extends RegexTest
{
	private String oldVersion;
	private SimpleResponse response;
	private WikiPage root;
	private WikiPage page;

	private void makeTestResponse(String pageName) throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		page = root.getPageCrawler().addPage(root, PathParser.parse(pageName), "original content");
		PageData data = page.getData();
		data.setContent("new stuff");
		VersionInfo commitRecord = page.commit(data);
		oldVersion = commitRecord.getName();

		MockRequest request = new MockRequest();
		request.setResource(pageName);
		request.addInput("version", oldVersion);

		Responder responder = new VersionResponder();
		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
	}

	public void testVersionName() throws Exception
	{
		makeTestResponse("PageOne");

		assertHasRegexp("original content", response.getContent());
		assertDoesntHaveRegexp("new stuff", response.getContent());
		assertHasRegexp(oldVersion, response.getContent());
	}

	public void testButtons() throws Exception
	{
		makeTestResponse("PageOne");

		assertDoesntHaveRegexp("Edit button", response.getContent());
		assertDoesntHaveRegexp("Search button", response.getContent());
		assertDoesntHaveRegexp("Test button", response.getContent());
		assertDoesntHaveRegexp("Suite button", response.getContent());
		assertDoesntHaveRegexp("Versions button", response.getContent());

		assertHasRegexp("Rollback button", response.getContent());
	}

	public void testNameNoAtRootLevel() throws Exception
	{
		makeTestResponse("PageOne.PageTwo");
		assertSubString("PageOne.PageTwo?responder=", response.getContent());
	}
}
