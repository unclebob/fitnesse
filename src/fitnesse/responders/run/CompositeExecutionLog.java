// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.util.HashMap;
import java.util.Map;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class CompositeExecutionLog {
  private WikiPagePath errorLogPagePath;
  private PageCrawler crawler;
  private WikiPage root;

  public CompositeExecutionLog(WikiPage testPage) {
    crawler = testPage.getPageCrawler();
    root = crawler.getRoot(testPage);
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    errorLogPagePath = crawler.getFullPath(testPage).addNameToFront(ExecutionLog.ErrorLogName);
  }

  private Map<String, ExecutionLog> logs = new HashMap<String, ExecutionLog>();

  public void add(String testSystemName, ExecutionLog executionLog) {
    logs.put(testSystemName, executionLog);
  }

  public void publish() {
    String content = buildLogContent();

    WikiPage errorLogPage = crawler.addPage(root, errorLogPagePath);
    PageData data = errorLogPage.getData();
    data.setContent(content);
    errorLogPage.commit(data);
  }

  private String buildLogContent() {
    StringBuffer logContent = new StringBuffer();
    for (String testSystemName : logs.keySet()) {
      logContent.append(String.format("!3 !-%s-!\n", testSystemName));
      logContent.append(logs.get(testSystemName).buildLogContent());
    }
    return logContent.toString();
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
}
