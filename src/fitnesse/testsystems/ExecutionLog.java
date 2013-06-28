// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fitnesse.responders.PageFactory;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.apache.velocity.VelocityContext;
import util.Clock;

public class ExecutionLog {

  private final CommandRunner runner;
  private final List<Throwable> exceptions = new LinkedList<Throwable>();

  public ExecutionLog(CommandRunner client) {
    runner = client;
  }

  public void addException(Throwable e) {
    exceptions.add(e);
  }

  int exceptionCount() {
    return exceptions.size() + runner.getExceptions().size();
  }

  public List<Throwable> getExceptions() {
    List<Throwable> exc = new ArrayList<Throwable>(exceptions);
    exc.addAll(runner.getExceptions());
    return exc;
  }

  boolean hasCapturedOutput() {
    return runner.wroteToErrorStream() || runner.wroteToOutputStream();
  }

  public String getCommand() {
    return runner.getCommand();
  }

  public long getExecutionTime() {
    return runner.getExecutionTime();
  }

  public int getExitCode() {
    return runner.getExitCode();
  }

  public String getCapturedOutput() {
    return runner.getOutput();
  }

  public String getCapturedError() {
    return runner.getError();
  }

}
