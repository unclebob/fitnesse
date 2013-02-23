// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import fitnesse.responders.PageFactory;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.apache.velocity.VelocityContext;
import util.Clock;

public class ExecutionLog {
  public static final String ErrorLogName = "ErrorLogs";
  private PageCrawler crawler;

  private final String errorLogPageName;
  private final WikiPagePath errorLogPagePath;

  private final WikiPage testPage;
  private final CommandRunner runner;
  private final List<Throwable> exceptions = new LinkedList<Throwable>();

  public ExecutionLog(WikiPage testPage, CommandRunner client) {
    this.testPage = testPage;
    runner = client;

    crawler = testPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    errorLogPagePath = crawler.getFullPath(testPage).addNameToFront(ErrorLogName);
    errorLogPageName = PathParser.render(errorLogPagePath);
  }

  void addException(Throwable e) {
    exceptions.add(e);
  }

  String buildLogContent(PageFactory pageFactory) {
    VelocityContext context = new VelocityContext();

    context.put("currentDate", makeDateFormat().format(Clock.currentDate()));
    context.put("testPage", "." + PathParser.render(crawler.getFullPath(testPage)));
    context.put("runner", runner);
    exceptions.addAll(runner.getExceptions());
    context.put("exceptions", exceptions);

    return pageFactory.render(context, "executionLog.vm");
  }

  int exceptionCount() {
    return exceptions.size();
  }

  String getErrorLogPageName() {
    return errorLogPageName;
  }

  boolean hasCapturedOutput() {
    return runner.wroteToErrorStream() || runner.wroteToOutputStream();
  }


  public int getExitCode() {
    return runner.getExitCode();
  }

  public CommandRunner getCommandRunner() {
    return runner;
  }

  private SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("h:mm:ss a (z) 'on' EEEE, MMMM d, yyyy");
  }
}
