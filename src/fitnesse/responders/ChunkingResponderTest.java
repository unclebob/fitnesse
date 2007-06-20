// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class ChunkingResponderTest extends RegexTest
{

	private Exception exception;
	private Response response;
	private FitNesseContext context;
	private WikiPage root = new WikiPageDummy();
	private ChunkingResponder responder = new ChunkingResponder()
	{
		protected void doSending() throws Exception
		{
			throw exception;
		}
	};

	protected void setUp() throws Exception
	{
		context = new FitNesseContext();
		context.root = root;
	}

	public void testException() throws Exception
	{
		exception = new Exception("test exception");
		response = responder.makeResponse(context, new MockRequest());
		String result = new MockResponseSender(response).sentData();
		assertSubString("test exception", result);
	}
}