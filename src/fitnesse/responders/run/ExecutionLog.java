// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import fitnesse.components.CommandRunner;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.responders.ErrorResponder;
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

  private String errorLogPageName;
  private WikiPagePath errorLogPagePath;
  private WikiPage root;

  private WikiPage testPage;
  private CommandRunner runner;
  private List<String> reasons = new LinkedList<String>();
  private List<Throwable> exceptions = new LinkedList<Throwable>();

  public ExecutionLog(WikiPage testPage, CommandRunner client) throws Exception {
    this.testPage = testPage;
    runner = client;

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

  public void publish() throws Exception {
    String content = buildLogContent();

    WikiPage errorLogPage = crawler.addPage(root, errorLogPagePath);
    PageData data = errorLogPage.getData();
    data.setContent(content);
    errorLogPage.commit(data);
  }

  String buildLogContent() throws Exception {
    StringBuffer buffer = new StringBuffer();
    addLiteralEntry(buffer, "Date", makeDateFormat().format(new Date()));
    addEntry(buffer, "Test Page", "." + PathParser.render(crawler.getFullPath(testPage)));
    addLiteralEntry(buffer, "Command", runner.getCommand());
    addLiteralEntry(buffer, "Exit code", String.valueOf(runner.getExitCode()));
    addLiteralEntry(buffer, "Time elapsed", (double) runner.getExecutionTime() / 1000.0 + " seconds");
    if (runner.wroteToOutputStream())
      addOutputBlock(buffer);
    if (runner.wroteToErrorStream())
      addErrorBlock(buffer);
    if (runner.hasExceptions() || exceptions.size() > 0)
      addExceptionBlock(buffer);
    return buffer.toString();
  }

  private void addLiteralEntry(StringBuffer buffer, String key, String value) {
    addEntry(buffer, key, literalize(value));
  }

  private void addEntry(StringBuffer buffer, String key, String value) {
    buffer.append("|'''").append(key).append(": '''|").append(value).append("|\n");
  }

  private String literalize(String s) {
    return String.format("!-%s-!", s);
  }

  private void addOutputBlock(StringBuffer buffer) {
    buffer.append("----");
    buffer.append("'''Standard Output:'''").append("\n");
    buffer.append("{{{").append(runner.getOutput()).append("}}}");
  }

  private void addErrorBlock(StringBuffer buffer) {
    buffer.append("----");
    buffer.append("'''Standard Error:'''").append("\n");
    buffer.append("{{{").append(runner.getError()).append("}}}");
  }

  private void addExceptionBlock(StringBuffer buffer) {
    exceptions.addAll(runner.getExceptions());
    buffer.append("----");
    buffer.append("'''Internal Exception");
    if (exceptions.size() > 1)
      buffer.append("s");
    buffer.append(":'''").append("\n");
    for (Throwable exception : exceptions) {
      buffer.append("{{{ ").append(ErrorResponder.makeExceptionString(exception)).append("}}}");
    }
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

  public String executionStatusHtml() throws Exception {
    String linkHref = getErrorLogPageName();
    return executionStatusHtml(linkHref);
  }

  private String executionStatusHtml(String linkHref) throws Exception {
    ExecutionStatus executionStatus;

    if (exceptionCount() > 0)
      executionStatus = ExecutionStatus.ERROR;
    else if (hasCapturedOutput())
      executionStatus = ExecutionStatus.OUTPUT;
    else
      executionStatus = ExecutionStatus.OK;

    return makeExecutionStatusLink(linkHref, executionStatus);
  }

  public static String makeExecutionStatusLink(String linkHref, ExecutionStatus executionStatus) throws Exception {
    HtmlTag status = new HtmlTag("div");
    status.addAttribute("id", "execution-status");
    HtmlTag image = new HtmlTag("img");
    image.addAttribute("src", "/files/images/executionStatus/" + executionStatus.getIconFilename());
    status.add(HtmlUtil.makeLink(linkHref, image.html()));
    status.add(HtmlUtil.BR);
    status.add(HtmlUtil.makeLink(linkHref, executionStatus.getMessage()));
    return status.html();
  }

  public int getExitCode() {
    return runner.getExitCode();
  }

  public CommandRunner getCommandRunner() {
    return runner;
  }
}
