package fitnesse.responders.run;

import fitnesse.wiki.*;

import java.util.HashMap;
import java.util.Map;

public class CompositeExecutionLog {
  private String errorLogPageName;
  private WikiPagePath errorLogPagePath;
  private PageCrawler crawler;
  private WikiPage root;

  public CompositeExecutionLog(WikiPage testPage) throws Exception {
    crawler = testPage.getPageCrawler();
    root = crawler.getRoot(testPage);
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    errorLogPagePath = crawler.getFullPath(testPage).addNameToFront(ExecutionLog.ErrorLogName);
    errorLogPageName = PathParser.render(errorLogPagePath);
  }

  private Map<String, ExecutionLog> logs = new HashMap<String, ExecutionLog>();

  public void add(String testSystemName, ExecutionLog executionLog) {
    logs.put(testSystemName, executionLog);
  }

  public void publish() throws Exception {
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

  public String executionStatusHtml() throws Exception {
    for (ExecutionLog log : logs.values())
      if (log.exceptionCount() != 0)
        return ExecutionLog.makeExecutionStatusLink(errorLogPageName, ExecutionStatus.ERROR);

    for (ExecutionLog log : logs.values())
      if (log.hasCapturedOutput())
        return ExecutionLog.makeExecutionStatusLink(errorLogPageName, ExecutionStatus.OUTPUT);

    return ExecutionLog.makeExecutionStatusLink(errorLogPageName, ExecutionStatus.OK);
  }
}
