// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;

public class ErrorResponderTest extends RegexTest
{
	public void testResponse() throws Exception
	{
		Responder responder = new ErrorResponder(new Exception("some error message"));
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), new MockRequest());

		assertEquals(400, response.getStatus());

		String body = response.getContent();

		assertHasRegexp("<html>", body);
		assertHasRegexp("<body", body);
		assertHasRegexp("java.lang.Exception: some error message", body);
	}

	public void testWithMessage() throws Exception
	{
		Responder responder = new ErrorResponder("error Message");
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), new MockRequest());
		String body = response.getContent();

		assertSubString("error Message", body);
	}
}
