// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.testutil.*;
import fitnesse.wiki.*;

public class ErrorLogGeneratorTest extends RegexTest
{
	private static String ErrorLogName = ExecutionLog.ErrorLogName;

	private WikiPage testPage;
	private MockCommandRunner runner;
	private ExecutionLog log;
	private WikiPage root;
	private WikiPage errorLogsParentPage;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		errorLogsParentPage = root.addChildPage(ErrorLogName);
		testPage = root.addChildPage("TestPage");
		runner = new MockCommandRunner("some command", 123);
		log = new ExecutionLog(testPage, runner);
	}

	public void tearDown() throws Exception
	{
	}

	public void testPageIsCreated() throws Exception
	{
		log.publish();
		assertTrue(errorLogsParentPage.hasChildPage(testPage.getName()));
	}

	public void testErrorLogContentIsReplaced() throws Exception
	{
		WikiPage errorLogPage = root.getPageCrawler().addPage(root, PathParser.parse("ErrorLogs.TestPage"));
		PageData data = errorLogPage.getData();
		data.setContent("old content");
		errorLogPage.commit(data);

		log.publish();
		String content = errorLogPage.getData().getContent();
		assertNotSubString("old content", content);
	}

	public void testBasicContent() throws Exception
	{
		String content = getGeneratedContent();

		assertSubString("'''Command: '''", content);
		assertSubString("!-some command-!", content);
		assertSubString("'''Exit code: '''", content);
		assertSubString("123", content);
		assertSubString("'''Date: '''", content);
		assertSubString("'''Time elapsed: '''", content);
	}

	private String getGeneratedContent() throws Exception
	{
		log.publish();
		WikiPage errorLogPage = errorLogsParentPage.getChildPage(testPage.getName());
		String content = errorLogPage.getData().getContent();
		return content;
	}

	public void testNoExtraLogTextWasGenerated() throws Exception
	{
		String content = getGeneratedContent();

		assertNotSubString("Exception", content);
		assertNotSubString("Standard Error", content);
		assertNotSubString("Standard Output", content);
	}

	public void testStdout() throws Exception
	{
		runner.setOutput("standard output that got printed");
		String content = getGeneratedContent();

		assertSubString("'''Standard Output:'''", content);
		assertSubString("standard output that got printed", content);
	}

	public void testStderr() throws Exception
	{
		runner.setError("standard error that got printed");
		String content = getGeneratedContent();

		assertSubString("'''Standard Error:'''", content);
		assertSubString("standard error that got printed", content);
	}

	public void testException() throws Exception
	{
		log.addException(new Exception("I made this"));
		String content = getGeneratedContent();

		assertSubString("'''Internal Exception:'''", content);
		assertSubString("I made this", content);
	}

	public void testExecutionReport_Ok() throws Exception
	{
		WikiPageDummy wikiPageDummy = new WikiPageDummy("This.Is.Not.A.Real.Location");
		MockCommandRunner mockCommandRunner = new MockCommandRunner();
		ExecutionLog executionLog = new ExecutionLog(wikiPageDummy, mockCommandRunner);
		ExecutionStatus result;

		if(executionLog.exceptionCount() > 0)
			result = ExecutionStatus.ERROR;
		else if(executionLog.hasCapturedOutput())
			result = ExecutionStatus.OUTPUT;
		else
			result = ExecutionStatus.OK;

		assertSame(ExecutionStatus.OK, result);
	}

	public void testExecutionReport_Output() throws Exception
	{
		WikiPageDummy wikiPageDummy = new WikiPageDummy("This.Is.Not.A.Real.Location");
		MockCommandRunner mockCommandRunner = new MockCommandRunner();
		mockCommandRunner.setOutput("I wrote something here");
		ExecutionLog executionLog = new ExecutionLog(wikiPageDummy, mockCommandRunner);
		ExecutionStatus result;

		if(executionLog.exceptionCount() > 0)
			result = ExecutionStatus.ERROR;
		else if(executionLog.hasCapturedOutput())
			result = ExecutionStatus.OUTPUT;
		else
			result = ExecutionStatus.OK;

		assertSame(ExecutionStatus.OUTPUT, result);
	}

	public void testExecutionReport_Error() throws Exception
	{
		WikiPageDummy wikiPageDummy = new WikiPageDummy("This.Is.Not.A.Real.Location");
		MockCommandRunner mockCommandRunner = new MockCommandRunner();
		ExecutionLog executionLog = new ExecutionLog(wikiPageDummy, mockCommandRunner);
		executionLog.addException(new RuntimeException("I messed up"));
		ExecutionStatus result;

		if(executionLog.exceptionCount() > 0)
			result = ExecutionStatus.ERROR;
		else if(executionLog.hasCapturedOutput())
			result = ExecutionStatus.OUTPUT;
		else
			result = ExecutionStatus.OK;

		assertSame(ExecutionStatus.ERROR, result);
	}
}

