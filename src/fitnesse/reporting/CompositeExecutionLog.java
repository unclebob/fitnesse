// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import fitnesse.html.HtmlElement;
import fitnesse.html.template.PageFactory;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.wiki.*;
import org.apache.velocity.VelocityContext;
import util.Clock;

import static fitnesse.html.HtmlElement.endl;

public class CompositeExecutionLog implements ExecutionLogListener {

  private final WikiPage testPage;
  private final String testPagePath;
  private WikiPagePath errorLogPagePath;

  public CompositeExecutionLog(WikiPage testPage) {
    this.testPage = testPage;
    PageCrawler crawler = testPage.getPageCrawler();
    testPagePath = "." + crawler.getFullPath();
    errorLogPagePath = crawler.getFullPath().addNameToFront(PageData.ErrorLogName);
  }

  private Map<String, ExecutionLog> logs = new HashMap<String, ExecutionLog>();

  public void add(String testSystemName, ExecutionLog executionLog) {
    logs.put(testSystemName, executionLog);
  }

  public void publish(PageFactory pageFactory) {
    String content = buildLogContent(pageFactory);
    PageCrawler crawler = testPage.getPageCrawler();
    WikiPage root = crawler.getRoot();

    WikiPage errorLogPage = WikiPageUtil.addPage(root, errorLogPagePath);
    PageData data = errorLogPage.getData();

    if(root != null) {
      WikiPagePath wpp = new WikiPagePath(errorLogPagePath.getRest());
      WikiPage wikiPage = root.getPageCrawler().getPage(wpp);
      if(wikiPage != null) {
        PageData pageData = wikiPage.getData();
        String tags = pageData.getAttribute(PageData.PropertySUITES);
        if(tags != null && !"".equals(tags)){
          data.setAttribute(PageData.PropertySUITES,tags);
        }
      }
    }

    data.setContent(content);
    errorLogPage.commit(data);
  }

  String buildLogContent(PageFactory pageFactory) {
    VelocityContext context = new VelocityContext();

    context.put("currentDate", makeDateFormat().format(Clock.currentDate()));
    context.put("testPage", testPagePath);
    context.put("logs", logs);

    return pageFactory.render(context, "executionLog.vm");
  }

  public String getErrorLogPageName() {
    return PathParser.render(errorLogPagePath);
  }

  public int exceptionCount() {
    int count = 0;
    for (ExecutionLog log : logs.values())
      count += log.getExceptions().size();
    return count;
  }

  public boolean hasCapturedOutput() {
    for (ExecutionLog log : logs.values())
      if (log.hasCapturedOutput())
        return true;
    return false;
  }

  private SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("h:mm:ss a (z) 'on' EEEE, MMMM d, yyyy");
  }

  @Override
  public void commandStarted(ExecutionContext context) {
    executionLog = new ExecutionLog();
    executionLog.command = context.getCommand();
    startTime = Clock.currentTimeInMillis();
    logs.put(context.getTestSystemName() + logs.size(), executionLog);
  }

  @Override
  public void stdOut(String output) {
    executionLog.capturedOutput.append(output).append("\n");
  }

  @Override
  public void stdErr(String output) {
    executionLog.capturedError.append(output).append("\n");
  }

  @Override
  public void exitCode(int exitCode) {
    long endTime = Clock.currentTimeInMillis();
    executionLog.executionTime = endTime - startTime;
    executionLog.exitCode = exitCode;
  }

  @Override
  public void exceptionOccurred(Throwable e) {
    executionLog.exceptions.add(e);
  }

  public static class ExecutionLog {
    private String command = "";
    private long executionTime;
    private int exitCode;
    private StringBuilder capturedOutput = new StringBuilder();
    private StringBuilder capturedError = new StringBuilder();
    private List<Throwable> exceptions = new LinkedList<Throwable>();

    public String getCommand() {
      return command;
    }

    public long getExecutionTime() {
      return executionTime;
    }

    public int getExitCode() {
      return exitCode;
    }

    public String getCapturedOutput() {
      return capturedOutput.toString();
    }

    public String getCapturedError() {
      return capturedError.toString();
    }

    public List<Throwable> getExceptions() {
      return exceptions;
    }

  }
}
