// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class TableWizardResponderTest extends RegexTest
{
	private WikiPage root;
	private MockRequest request;
	private Responder responder;
	private SimpleResponse response;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("root");
		crawler = root.getPageCrawler();
		request = new MockRequest();
		responder = new TableWizardResponder();
	}

	public void testResponseForNonFixture() throws Exception
	{
		prepareTest();
		request.setResource("ChildPage");
		request.addInput("fixture", "fitnesse.FitNesse");
		request.addInput("text", "child content");

		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		String body = response.getContent();
		assertMatches("child content", body);
		assertMatches("# fitnesse.FitNesse is not a valid fixture! #", body);
	}

	public void testResponseForNonColumnFixture() throws Exception
	{
		crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
		request.setResource("ChildPage");
		request.addInput("fixture", "fitnesse.FitNesse");
		request.addInput("text", "child content with <html>\nmore text.\n");

		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertEquals(200, response.getStatus());

		String body = response.getContent();
		assertTrue(body.indexOf("<html>") != -1);
		assertTrue(body.indexOf("<form") != -1);
		assertTrue(body.indexOf("method=\"post\"") != -1);
		assertTrue(body.indexOf("child content with &lt;html&gt;") != -1);
		assertTrue(body.indexOf("name=\"saveId\"") != -1);
		assertMatches("child content with &lt;html&gt;.*more text..*!-fitnesse.FitNesse-!|", body);
	}

	public void testResponseForColumnFixture() throws Exception
	{
		prepareTest();
		request.setResource("ChildPage");
		request.addInput("fixture", "fitnesse.testutil.DummyClassForWizardTest");
		request.addInput("text", "child content");

		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		String body = response.getContent();
		assertTrue(body.indexOf("child content\n!|fitnesse.testutil.DummyClassForWizardTest|\n|v1 |f1()|\n") != -1);
		assertTrue(body.indexOf("|int|int |") != -1);
	}

	public void testResponseForRowFixture() throws Exception
	{
		prepareTest();
		request.setResource("ChildPage");
		request.addInput("fixture", "fitnesse.fixtures.PayCheckRecordFixture");
		request.addInput("text", "child content");

		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		String body = response.getContent();
		assertTrue(body.indexOf("child content\n!|fitnesse.fixtures.PayCheckRecordFixture|\n|employeeId|date  |name  |pay() |\n") != -1);
		assertTrue(body.indexOf("|int       |String|String|double|\n") != -1);
	}

	public void testCreateCommandLine() throws Exception
	{
		prepareTest();
		String commandLine = new TableWizardResponder().createCommandLine(root.getChildPage("ChildPage"), "fitnesse.fixtures.ResponseExaminer");
		assertTrue(commandLine.startsWith("java -cp "));
		assertTrue(commandLine.endsWith("fitnesse.FixtureTemplateCreator fitnesse.fixtures.ResponseExaminer"));
	}

	private void prepareTest() throws Exception
	{
		crawler.addPage(root, PathParser.parse("ChildPage"), "child content");
		PageData data = root.getData();
		data.setContent("!path classes");
		root.commit(data);
	}
}