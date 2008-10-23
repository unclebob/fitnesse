package fitnesse.responders.run;

import fitnesse.wiki.*;

import java.util.HashMap;
import java.util.Map;

public class CompositeExecutionLog {
  private String errorLogPageName;

  public CompositeExecutionLog(WikiPage testPage) throws Exception {
    PageCrawler crawler = testPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    WikiPagePath errorLogPagePath = crawler.getFullPath(testPage).addNameToFront(ExecutionLog.ErrorLogName);
    errorLogPageName = PathParser.render(errorLogPagePath);
  }

  private Map<String, ExecutionLog> logs = new HashMap<String, ExecutionLog>();

  public void add(String testSystemName, ExecutionLog executionLog) {
    logs.put(testSystemName, executionLog);
  }

  public void publish() throws Exception {
    for (ExecutionLog log : logs.values())
      log.publish();
  }

  public String getExitCode() {
    if (logs.size() == 1) {
      ExecutionLog log = logs.values().toArray(new ExecutionLog[1])[0];
      return String.valueOf(log.getExitCode());
    } else {
      return errorCodeList();
    }
  }

  private String errorCodeList() {
    StringBuffer exitCodeList = new StringBuffer();
    exitCodeList.append("[");
    for (ExecutionLog log : logs.values()) {
      exitCodeList.append(String.valueOf(log.getExitCode()));
      exitCodeList.append(",");
    }
    exitCodeList.deleteCharAt(exitCodeList.length() - 1);
    exitCodeList.append("]");
    return exitCodeList.toString();
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
