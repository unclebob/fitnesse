// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import fitnesse.responders.PageFactory;
import fitnesse.wiki.*;
import org.apache.velocity.VelocityContext;
import util.Clock;

public class CompositeExecutionLog {
  public static final String ErrorLogName = "ErrorLogs";

  private final WikiPage testPage;
  private final String testPagePath;
  private WikiPagePath errorLogPagePath;

  public CompositeExecutionLog(WikiPage testPage) {
    this.testPage = testPage;
    PageCrawler crawler = testPage.getPageCrawler();
    testPagePath = "." + crawler.getFullPath();
    errorLogPagePath = crawler.getFullPath().addNameToFront(ErrorLogName);
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
        if(tags != null && tags !="" ){
          data.setAttribute(PageData.PropertySUITES,tags);
        }
      }
    }

    data.setContent(content);
    errorLogPage.commit(data);
  }

  private String buildLogContent(PageFactory pageFactory) {
    StringBuffer logContent = new StringBuffer();
    for (String testSystemName : logs.keySet()) {
      logContent.append(String.format("!3 !-%s-!\n", testSystemName));
      logContent.append(buildLogContent(logs.get(testSystemName), pageFactory));
    }
    return logContent.toString();
  }

  String buildLogContent(ExecutionLog log, PageFactory pageFactory) {
    VelocityContext context = new VelocityContext();

    context.put("currentDate", makeDateFormat().format(Clock.currentDate()));
    context.put("testPage", testPagePath);
    context.put("log", log);

    return pageFactory.render(context, "executionLog.vm");
  }

  public String getErrorLogPageName() {
    return PathParser.render(errorLogPagePath);
  }

  public int exceptionCount() {
    int count = 0;
    for (ExecutionLog log : logs.values())
      count += log.exceptionCount();
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
}
