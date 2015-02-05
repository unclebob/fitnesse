// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.html.template.PageFactory;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.wiki.*;
import org.apache.velocity.VelocityContext;
import fitnesse.util.Clock;

public class CompositeExecutionLog implements ExecutionLogListener {

  private final String testPagePath;
  private ExecutionLog executionLog;
  private Map<String, ExecutionLog> logs = new HashMap<String, ExecutionLog>();
  private long startTime;

  public CompositeExecutionLog(WikiPage testPage) {
    PageCrawler crawler = testPage.getPageCrawler();
    testPagePath = "." + crawler.getFullPath();
  }

  String buildLogContent(PageFactory pageFactory) {
    VelocityContext context = new VelocityContext();

    context.put("currentDate", makeDateFormat().format(Clock.currentDate()));
    context.put("testPage", testPagePath);
    context.put("logs", logs);

    return pageFactory.render(context, "executionLog.vm");
  }

  public int exceptionCount() {
    int count = 0;
    for (ExecutionLog log : logs.values())
      count += log.getExceptions().size();
    return count;
  }

  public boolean hasCapturedOutput() {
    for (ExecutionLog log : logs.values())
      if (!"".equals(log.getCapturedOutput()))
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
    logs.put(context.getTestSystemName(), executionLog);
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
