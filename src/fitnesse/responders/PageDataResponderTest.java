// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class PageDataResponderTest extends RegexTest
{
	WikiPage root;
	WikiPage pageOne;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		pageOne = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "Line one\nLine two");
	}

	public void testGetPageData() throws Exception
	{
		Responder responder = new PageDataWikiPageResponder();
		MockRequest request = new MockRequest();
		request.setResource("PageOne");
		request.addInput("pageData", "");
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(pageOne.getData().getContent(), response.getContent());
	}
}
