// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.html.*;
import fitnesse.responders.*;
import fitnesse.components.*;
import fitnesse.wiki.*;
import fitnesse.http.*;
import fitnesse.authentication.*;
import fit.Counts;
import java.util.*;

public class TestResponder extends ChunkingResponder implements FitClientListener, SecureResponder
{
	protected static final String emptyPageContent = "OH NO! This page is empty!";

	protected HtmlPage html;
	protected CommandRunningFitClient client;
	protected String command;
	protected ExecutionLog log;
	protected PageData data;
	private boolean closed = false;
	public static final String DEFAULT_COMMAND_PATTERN = "java -cp %p %m";
	protected static final int htmlDepth = 2;
	private Counts assertionCounts = new Counts();
	protected TestHtmlFormatter formatter;

	protected void doSending() throws Exception
	{
		data = page.getData();

		buildHtml();
		addToResponse(formatter.head());
		addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));

		String testableHtml = HtmlUtil.testableHtml(data);
		if(testableHtml.length() == 0)
			testableHtml = handleBlankHtml();
		String classPath = new ClassPathBuilder().getClasspath(page);
		command = buildCommand(data, getClassName(data, request), classPath);
		client = new CommandRunningFitClient(this, command, context.port, context.socketDealer);
		log = new ExecutionLog(page, client.commandRunner);

		client.start();
		if(client.isSuccessfullyStarted())
		{
			client.send(testableHtml);
			client.done();
			client.join();
		}

		completeResponse();
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
			client.kill();
			log.addException(e);
			log.addReason("Test execution aborted abnormally with error code " + client.commandRunner.getExitCode());

			completeResponse();
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

	protected void addToResponse(String output) throws Exception
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
}
