// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.*;

import java.util.HashMap;
import java.util.Map;

public class CompositeExecutionLog {
  private WikiPagePath errorLogPagePath;
  private PageCrawler crawler;
  private WikiPage root;

  public CompositeExecutionLog(WikiPage testPage) {
    crawler = testPage.getPageCrawler();
    root = crawler.getRoot(testPage);
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
    
    if(root != null) {
      WikiPagePath wpp = new WikiPagePath(errorLogPagePath.getRest());
      WikiPage wikiPage = crawler.getPage(root, wpp);
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
