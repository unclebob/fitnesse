// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import util.Clock;

import fitnesse.components.CommandRunner;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.html.TagGroup;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.PageFactory;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class ExecutionLog {
  public static final String ErrorLogName = "ErrorLogs";
  private PageCrawler crawler;

  private static SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("h:mm:ss a (z) 'on' EEEE, MMMM d, yyyy");
  }

  private final String errorLogPageName;
  private final WikiPagePath errorLogPagePath;
  private final WikiPage root;

  private final WikiPage testPage;
  private final CommandRunner runner;
  private final PageFactory pageFactory;
  private final List<String> reasons = new LinkedList<String>();
  private final List<Throwable> exceptions = new LinkedList<Throwable>();

  public ExecutionLog(WikiPage testPage, CommandRunner client, PageFactory pageFactory) {
    this.testPage = testPage;
    runner = client;
    this.pageFactory = pageFactory;
    
    crawler = testPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    root = crawler.getRoot(testPage);
    errorLogPagePath = crawler.getFullPath(testPage).addNameToFront(ErrorLogName);
    errorLogPageName = PathParser.render(errorLogPagePath);
  }

  void addException(Throwable e) {
    exceptions.add(e);
  }

  void addReason(String reason) {
    if (!reasons.contains(reason))
      reasons.add(reason);
  }

  public void publish() {
    String content = buildLogContent();

    WikiPage errorLogPage = crawler.addPage(root, errorLogPagePath);
    PageData data = errorLogPage.getData();
    data.setContent(content);
    errorLogPage.commit(data);
  }

  String buildLogContent() {
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
}
