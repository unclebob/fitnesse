// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.testutil.*;
import fitnesse.wiki.*;
import fitnesse.http.*;
import fitnesse.*;

public abstract class ResponderTest extends RegexTest
{
	protected WikiPage root;
	protected MockRequest request;
	protected Responder responder;
	protected PageCrawler crawler;
	protected FitNesseContext context;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		request = new MockRequest();
		responder = responderInstance();
		context = new FitNesseContext(root);
	}

	// Return an instance of the Responder being tested.
	protected abstract Responder responderInstance();
}
