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
  public List<SlimTable> testTables = new ArrayList<SlimTable>();
  public TableScanner tableScanner;
  public Map<String, Object> instructionResults;
  public Map<String, String> exceptions;
  public List<Object> instructions;

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
    runTestsOnPage(pageData);
    String wikiText = generateWikiTextForTestResults();
    pageData.setContent(wikiText);
    testResults = pageData;
  }

  private String generateWikiTextForTestResults() throws Exception {
    replaceExceptionsWithLinks();
    putTestResultsIntoTables();
    return ExceptionList.toWikiText(exceptions) + testResultsToWikiText();
  }

  private void runTestsOnPage(PageData pageData) throws Exception {
    tableScanner = new TableScanner(pageData);
    instructions = createInstructions();
    instructionResults = slimClient.invokeAndGetResponse(instructions);
  }

  private void replaceExceptionsWithLinks() {
    exceptions = new HashMap<String, String>();
    Set<String> resultKeys = instructionResults.keySet();
    for (String resultKey : resultKeys)
      replaceExceptionWithExceptionLink(resultKey);
  }

  private void replaceExceptionWithExceptionLink(String resultKey) {
    Object result = instructionResults.get(resultKey);
    if (result instanceof String) {
      String resultString = (String) result;
      if (resultString.indexOf(SlimServer.EXCEPTION_TAG) != -1) {
        exceptions.put(resultKey, resultString);
        instructionResults.put(resultKey, exceptionResult(resultKey));
      }
    }
  }

  private String exceptionResult(String resultKey) {
    return String.format("Exception: .#%s", resultKey);
  }

  private String testResultsToWikiText() throws Exception {
    String wikiText = tableScanner.toWikiText() +
      "!* Standard Output\n\n" +
      slimRunner.getOutput() +
      "*!\n" +
      "!* Standard Error\n\n" +
      slimRunner.getError() +
      "*!\n";
    return wikiText;
  }

  private void putTestResultsIntoTables() {
    for (SlimTable table : testTables)
      table.evaluateExpectations(instructionResults);
  }

  private List<Object> createInstructions() {
    List<Object> instructions = new ArrayList<Object>();
    for (Table table : tableScanner) {
      String tableId = "" + testTables.size();
      SlimTable slimTable = makeSlimTable(table, tableId);
      slimTable.appendInstructions(instructions);
      testTables.add(slimTable);
    }
    return instructions;
  }

  private SlimTable makeSlimTable(Table table, String tableId) {
    String tableType = table.getCellContents(0, 0);
    if (tableType.startsWith("DT:"))
      return new DecisionTable(table, tableId);
    else if (tableType.equalsIgnoreCase("import"))
      return new ImportTable(table, tableId);
    else
      return new SlimErrorTable(table, tableId);
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
    while (!isConnected(slimClient))
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

  public List<Object> getInstructions() {
    return instructions;
  }

  static class ExceptionList {
    private Map<String, String> exceptions;
    public StringBuffer buffer;
    public Set<String> keys;

    private ExceptionList(Map<String, String> exceptions) {
      this.exceptions = exceptions;
      buffer = new StringBuffer();
      keys = exceptions.keySet();
    }

    private String toWikiText() {
      header();
      exceptions();
      footer();
      return buffer.toString();
    }

    private void footer() {
      if (keys.size() > 0)
        buffer.append("----\n");
    }

    private void exceptions() {
      for (String key : keys) {
        buffer.append("!anchor " + key + "\n");
        buffer.append("!*> " + key + "\n");
        buffer.append("{{{ " + exceptions.get(key) + "}}}\n*!\n\n");
      }
    }

    private void header() {
      if (keys.size() > 0) {
        buffer.append("!3 !style_fail(Exceptions)\n");
      }
    }

    private static String toWikiText(Map<String, String> exceptions) {
      return new ExceptionList(exceptions).toWikiText();
    }
  }
}

