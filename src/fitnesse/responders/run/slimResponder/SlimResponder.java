package fitnesse.responders.run.slimResponder;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.components.CommandRunner;
import fitnesse.responders.WikiPageResponder;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimServer;
import fitnesse.wiki.PageData;

import java.util.*;

public class SlimResponder extends WikiPageResponder {
  private boolean slimOpen = false;
  private CommandRunner slimRunner;
  private SlimClient slimClient;
  private PageData testResults;
  public List<DecisionTable> testTables = new ArrayList<DecisionTable>();

  /* hook for subclasses */
  protected void processWikiPageDataBeforeGeneratingHtml(PageData pageData) throws Exception {
    startSlimConnection();
    processWikiText(pageData);
    closeSlimConnection();
  }

  private void closeSlimConnection() throws Exception {
    slimClient.sendBye();
    slimRunner.join();
    slimOpen = false;
  }

  private void processWikiText(PageData pageData) throws Exception {
    testResults = pageData;
    TableScanner tableScanner = new TableScanner(pageData);
    List<Object> instructions = createInstructions(tableScanner);
    Map<String, Object> results = slimClient.invokeAndGetResponse(instructions);
    Map<String, String> exceptions = mapExceptionsToLinks(results);
    evaluateResults(results);
    String wikiText = exceptionList(exceptions) + testResultsToWikiText(pageData, tableScanner);
    pageData.setContent(wikiText);
  }

  private String exceptionList(Map<String, String> exceptions) {
    StringBuffer exceptionList = new StringBuffer();
    Set<String> keys = exceptions.keySet();
    if (keys.size() > 0) {
      exceptionList.append("!3 !style_fail(Exceptions)\n");
    }
    for (String key : keys) {
      exceptionList.append("!anchor " + key + "\n");
      exceptionList.append("!* " + key + "\n");
      exceptionList.append("{{{ " + exceptions.get(key) + "}}}\n*!\n\n");
    }
    if (keys.size() > 0)
      exceptionList.append("----\n");
    return exceptionList.toString();
  }

  private Map<String, String> mapExceptionsToLinks(Map<String, Object> results) {
    Map<String, String> exceptions = new HashMap<String, String>();
    Set<String> keys = results.keySet();
    for (String key : keys) {
      Object value = results.get(key);
      if (value instanceof String) {
        String valueString = (String) value;
        if (valueString.indexOf(SlimServer.EXCEPTION_TAG) != -1) {
          exceptions.put(key, valueString);
          results.put(key, String.format("Exception: .#%s", key));
        }
      }
    }
    return exceptions;
  }

  private String testResultsToWikiText(PageData pageData, TableScanner tableScanner) throws Exception {
    String wikiText = tableScanner.toWikiText() +
        "!* Standard Output\n\n" +
        slimRunner.getOutput() +
        "*!\n" +
        "!* Standard Error\n\n" +
        slimRunner.getError() +
        "*!\n";
    return wikiText;
  }

  private void evaluateResults(Map<String, Object> results) {
    for (DecisionTable table : testTables)
      table.evaluateExpectations(results);
  }

  private List<Object> createInstructions(TableScanner tableScanner) {
    List<Object> instructions = new ArrayList<Object>();
    for (Table table : tableScanner) {
      DecisionTable dt = new DecisionTable(table, "id");
      dt.appendInstructionsTo(instructions);
      testTables.add(dt);
    }
    return instructions;
  }

  private void startSlimConnection() throws Exception {
    slimOpen = true;
    String classPath = new ClassPathBuilder().getClasspath(page);
    String slimCommand = String.format("java -cp %s fitnesse.slim.SlimService 8085", classPath);
    slimRunner = new CommandRunner(slimCommand, "");
    slimRunner.start();
    slimClient = new SlimClient("localhost", 8085);
    waitForConnection(slimClient);
  }

  private void waitForConnection(SlimClient slimClient) throws Exception {
    while (isConnected(slimClient) == false)
      Thread.sleep(50);
  }

  private boolean isConnected(SlimClient slimClient) throws Exception {
    try {
      slimClient.connect();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  boolean slimOpen() {
    return slimOpen;
  }

  public PageData getTestResults() {
    return testResults;
  }
}
