// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.authentication.*;
import fitnesse.components.*;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.responders.*;
import fitnesse.wiki.*;

import java.util.*;

public class TestResponder extends ChunkingResponder implements FitClientListener, SecureResponder
{
	protected static final String emptyPageContent = "OH NO! This page is empty!";
	public static final String DEFAULT_COMMAND_PATTERN = "java -cp %p %m";
	protected static final int htmlDepth = 2;

	private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();

	protected HtmlPage html;
	protected CommandRunningFitClient client;
	protected String command;
	protected ExecutionLog log;
	protected PageData data;
	private boolean closed = false;
	private Counts assertionCounts = new Counts();
	protected TestHtmlFormatter formatter;
	protected String classPath;
	private String testableHtml;

	protected void doSending() throws Exception
	{
		data = page.getData();
		startHtml();

		sendPreTestNotification();

		prepareForExecution();

		startFitClient(classPath);

		if(client.isSuccessfullyStarted())
			performExecution();

		finishSending();
	}

	private void sendPreTestNotification() throws Exception
	{
		for(Iterator iterator = eventListeners.iterator(); iterator.hasNext();)
		{
			TestEventListener testEventListener = (TestEventListener) iterator.next();
			testEventListener.notifyPreTest(this, data);
		}
	}

	protected void finishSending() throws Exception
	{
		completeResponse();
	}

	protected void performExecution() throws Exception
	{
		client.send(testableHtml);
		client.done();
		client.join();
	}

	protected void prepareForExecution() throws Exception
	{
		addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));

		testableHtml = HtmlUtil.testableHtml(data, true);
		if(testableHtml.length() == 0)
			testableHtml = handleBlankHtml();
		classPath = new ClassPathBuilder().getClasspath(page);
	}

	protected void startHtml() throws Exception
	{
		buildHtml();
		addToResponse(formatter.head());
	}

	protected void startFitClient(String classPath) throws Exception
	{
		command = buildCommand(data, getClassName(data, request), classPath);
		client = new CommandRunningFitClient(this, command, context.port, context.socketDealer);
		log = new ExecutionLog(page, client.commandRunner);

		client.start();
	}

	protected PageCrawler getPageCrawler()
	{
		PageCrawler crawler = root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		return crawler;
	}

	public void acceptResults(Counts counts) throws Exception
	{
		assertionCounts.tally(counts);
	}

	public synchronized void exceptionOccurred(Exception e)
	{
		try
		{
			log.addException(e);
			log.addReason("Test execution aborted abnormally with error code " + client.commandRunner.getExitCode());

			completeResponse();
			client.kill();
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

	protected synchronized void completeResponse() throws Exception
	{
		if(!closed)
		{
			closed = true;
			log.publish();
			addLogAndClose();
		}
	}

	protected final void addLogAndClose() throws Exception
	{
		addLog();
		close();
	}

	protected void close() throws Exception
	{
		response.add(HtmlUtil.getHtmlOfInheritedPage("PageFooter", page));
		response.add(formatter.tail());
		response.closeChunks();
		response.addTrailingHeader("Exit-Code", String.valueOf(client.commandRunner.getExitCode()));
		response.closeTrailer();
		response.close();
	}

	protected void addLog() throws Exception
	{
		response.add(formatter.testSummary(assertionCounts));
		response.add(formatter.executionStatus(log));
	}

	public void addToResponse(String output) throws Exception
	{
		if(!closed)
			response.add(output);
	}

	public void acceptOutput(String output) throws Exception
	{
		response.add(output);
	}

	private String handleBlankHtml() throws Exception
	{
		response.add(formatter.messageForBlankHtml());
		return emptyPageContent;
	}

	protected void buildHtml() throws Exception
	{
		PageCrawler pageCrawler = page.getPageCrawler();
		WikiPagePath fullPath = pageCrawler.getFullPath(page);
		String fullPathName = PathParser.render(fullPath);
		html = context.htmlPageFactory.newPage();
		html.title.use(pageType() + ": " + fullPathName);
		html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(fullPathName, pageType()));
		html.actions.use(HtmlUtil.makeActions(data));
		WikiImportProperty.handleImportProperties(html, page, data);

		makeFormatter();
	}

	protected void makeFormatter() throws Exception
	{
		formatter = new TestHtmlFormatter(html);
	}

	protected String pageType()
	{
		return "Test Results";
	}

	protected String title() throws Exception
	{
		WikiPagePath fullPath = getPageCrawler().getFullPath(page);
		TagGroup group = new TagGroup();
		group.add(HtmlUtil.makeLink(PathParser.render(fullPath), page.getName()));
		group.add(HtmlUtil.makeItalic(pageType()));
		return group.html();
	}

	public String getClassName(PageData data, Request request) throws Exception
	{
		String program = (String) request.getInput("className");
		if(program == null)
			program = data.getVariable("TEST_RUNNER");
		if(program == null)
			program = "fit.FitServer";
		return program;
	}

	protected String buildCommand(PageData data, String program, String classPath) throws Exception
	{
		String testRunner = data.getVariable("COMMAND_PATTERN");
		if(testRunner == null)
			testRunner = DEFAULT_COMMAND_PATTERN;
		String command = replace(testRunner, "%p", classPath);
		command = replace(command, "%m", program);
		return command;
	}

	// String.replaceAll(...) is not trustworthy because it seems to remove all '\' characters.
	protected String replace(String value, String mark, String replacement)
	{
		int index = value.indexOf(mark);
		if(index == -1)
			return value;

		return value.substring(0, index) + replacement + value.substring(index + mark.length());
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureTestOperation();
	}

	protected String cssClassFor(Counts count)
	{
		if(count.wrong > 0)
			return "fail";
		else if(count.exceptions > 0 || count.right + count.ignores == 0)
			return "error";
		else if(count.ignores > 0 && count.right == 0)
			return "ignore";
		else
			return "pass";
	}

	protected int exitCode()
	{
		return assertionCounts.wrong + assertionCounts.exceptions;
	}

	public static void registerListener(TestEventListener listener)
	{
		eventListeners.add(listener);
	}
}
