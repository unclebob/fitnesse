// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;

public class SearchFormResponderTest extends RegexTest
{
	private SimpleResponse response;
	private String content;

	public void setUp() throws Exception
	{
		SearchFormResponder responder = new SearchFormResponder();
		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), new MockRequest());
		content = response.getContent();
	}

	public void tearDown() throws Exception
	{
	}

	public void testFocusOnSearchBox() throws Exception
	{
		assertSubString("onload=\"document.forms[0].searchString.focus()\"", content);
	}

	public void testHtml() throws Exception
	{
		assertHasRegexp("form", content);
		assertHasRegexp("input", content);
		assertSubString("<input", content);
		assertSubString("type=\"hidden\"", content);
		assertSubString("name=\"responder\"", content);
		assertSubString("value=\"search\"", content);
	}

	public void testForTwoSearchTypes() throws Exception
	{
		assertSubString("type=\"submit\"", content);
		assertSubString("value=\"Search Titles!\"", content);
		assertSubString("value=\"Search Content!\"", content);
	}
}
