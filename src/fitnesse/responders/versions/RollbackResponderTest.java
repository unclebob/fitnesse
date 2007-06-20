// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.versions;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.wiki.*;
import junit.framework.TestCase;
import junit.swingui.TestRunner;

public class RollbackResponderTest extends TestCase
{
	private WikiPage root;
	private WikiPage page;
	private Response response;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"RollbackResponderTest"});
	}

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		page = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "original content");
		PageData data = page.getData();
		data.setContent("new stuff");
		data.setProperties(new WikiPageProperties());
		VersionInfo commitRecord = page.commit(data);

		MockRequest request = new MockRequest();
		request.setResource("PageOne");
		request.addInput("version", commitRecord.getName());

		Responder responder = new RollbackResponder();
		response = responder.makeResponse(new FitNesseContext(root), request);
	}

	public void tearDown() throws Exception
	{
	}

	public void testStuff() throws Exception
	{
		assertEquals(303, response.getStatus());
		assertEquals("PageOne", response.getHeader("Location"));

		PageData data = page.getData();
		assertEquals("original content", data.getContent());
		assertEquals(true, data.hasAttribute("Edit"));
	}
}
