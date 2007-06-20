// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.components.CommandRunner;
import fitnesse.html.*;
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ExecutionLog
{
	public static final String ErrorLogName = "ErrorLogs";
	private PageCrawler crawler;

	public static SimpleDateFormat makeDateFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat("h:mm:ss a (z) 'on' EEEE, MMMM d, yyyy");
	}

	private String errorLogPageName;
	private WikiPagePath errorLogPagePath;
	private WikiPage root;

	private CommandRunner runner;
	private List reasons = new LinkedList();
	private List exceptions = new LinkedList();

	public ExecutionLog(WikiPage testPage, CommandRunner client) throws Exception
	{
		runner = client;

		crawler = testPage.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		root = crawler.getRoot(testPage);
		errorLogPagePath = crawler.getFullPath(testPage).addNameToFront(ErrorLogName);
		errorLogPageName = PathParser.render(errorLogPagePath);
	}

	public void addException(Exception e)
	{
		exceptions.add(e);
	}

	public void addReason(String reason)
	{
		if(!reasons.contains(reason))
			reasons.add(reason);
	}

	public void publish() throws Exception
	{
		String content = buildLogContent();

		WikiPage errorLogPage = crawler.addPage(root, errorLogPagePath);
		PageData data = errorLogPage.getData();
		data.setContent(content);
		errorLogPage.commit(data);
	}

	public String buildLogContent()
	{
		StringBuffer buffer = new StringBuffer();
		addEntry(buffer, "Date", makeDateFormat().format(new Date()));
		addEntry(buffer, "Command", runner.getCommand());
		addEntry(buffer, "Exit code", String.valueOf(runner.getExitCode()));
		addEntry(buffer, "Time elapsed", (double) runner.getExecutionTime() / 1000.0 + " seconds");
		if(runner.wroteToOutputStream())
			addOutputBlock(buffer);
		if(runner.wroteToErrorStream())
			addErrorBlock(buffer);
		if(runner.hasExceptions() || exceptions.size() > 0)
			addExceptionBlock(buffer);
		String content = buffer.toString();
		return content;
	}

	private void addEntry(StringBuffer buffer, String key, String value)
	{
		buffer.append("|'''").append(key).append(": '''|").append("!-").append(value).append("-!").append("|\n");
	}

	private void addOutputBlock(StringBuffer buffer)
	{
		buffer.append("----");
		buffer.append("'''Standard Output:'''").append("\n");
		buffer.append("{{{").append(runner.getOutput()).append("}}}");
	}

	private void addErrorBlock(StringBuffer buffer)
	{
		buffer.append("----");
		buffer.append("'''Standard Error:'''").append("\n");
		buffer.append("{{{").append(runner.getError()).append("}}}");
	}

	private void addExceptionBlock(StringBuffer buffer)
	{
		exceptions.addAll(runner.getExceptions());
		buffer.append("----");
		buffer.append("'''Internal Exception");
		if(exceptions.size() > 1)
			buffer.append("s");
		buffer.append(":'''").append("\n");
		for(Iterator iterator = exceptions.iterator(); iterator.hasNext();)
		{
			Exception exception = (Exception) iterator.next();
			buffer.append("{{{ ").append(ErrorResponder.makeExceptionString(exception)).append("}}}");
		}
	}

	public int exceptionCount()
	{
		return exceptions.size();
	}

	public String getErrorLogPageName()
	{
		return errorLogPageName;
	}

	public boolean hasCapturedOutput()
	{
		return runner.wroteToErrorStream() || runner.wroteToOutputStream();
	}

	public String executionStatusHtml() throws Exception
	{
		String linkHref = getErrorLogPageName();
		return executionStatusHtml(linkHref, "");
	}

	public String executionStatusHtml(String linkHref, String imageUrlBase) throws Exception
	{
		ExecutionStatus executionStatus;

		if(exceptionCount() > 0)
			executionStatus = ExecutionStatus.ERROR;
		else if(hasCapturedOutput())
			executionStatus = ExecutionStatus.OUTPUT;
		else
			executionStatus = ExecutionStatus.OK;

		HtmlTag status = new HtmlTag("div");
		status.addAttribute("id", "execution-status");
		HtmlTag image = new HtmlTag("img");
		image.addAttribute("src", imageUrlBase + "/files/images/executionStatus/" + executionStatus.getIconFilename());
		status.add(HtmlUtil.makeLink(linkHref, image.html()));
		status.add(HtmlUtil.BR);
		status.add(HtmlUtil.makeLink(linkHref, executionStatus.getMessage()));
		return status.html();
	}
}
