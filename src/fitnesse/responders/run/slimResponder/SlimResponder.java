package fitnesse.responders.run.slimResponder;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.components.CommandRunner;
import fitnesse.responders.WikiPageResponder;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.wiki.PageData;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class SlimResponder extends WikiPageResponder implements SlimTestContext {
  private boolean slimOpen = false;
  private CommandRunner slimRunner;
  private SlimClient slimClient;
  private PageData testResults;
  private Map<String, String> symbols = new HashMap<String, String>();
  public List<SlimTable> testTables = new ArrayList<SlimTable>();
  public TableScanner tableScanner;
  public Map<String, Object> instructionResults;
  public Map<String, String> exceptions = new HashMap<String, String>();
  public List<Object> instructions;
  private String slimFlags;
  private String slimCommand;
  private boolean fastTest; // for testing only.  Makes responder much faster.


  protected void processWikiPageDataBeforeGeneratingHtml(PageData pageData) throws Exception {
    getSlimFlags(pageData);
    startSlimConnection();
    processWikiText(pageData);
    closeSlimConnection();
  }

  private void getSlimFlags(PageData pageData) throws Exception {
    slimFlags = pageData.getVariable("SLIM_FLAGS");
    if (slimFlags == null)
      slimFlags = "";
  }

  private void startSlimConnection() throws Exception {
    slimOpen = true;
    String classPath = new ClassPathBuilder().getClasspath(page);
    String slimArguments = String.format("%s %d", slimFlags, 8085);
    runSlim(classPath, slimArguments);
    slimClient = new SlimClient("localhost", 8085);
    waitForConnection(slimClient);
  }

  private void runSlim(String classPath, String slimArguments) throws Exception {
    slimCommand = String.format("java -cp %s fitnesse.slim.SlimService %s", classPath, slimArguments);
    if (fastTest) {
      createSlimService(slimArguments);
    } else {
      slimRunner = new CommandRunner(slimCommand, "");
      slimRunner.start();
    }
  }

  private void closeSlimConnection() throws Exception {
    slimClient.sendBye();
    if (!fastTest)
      slimRunner.join();
    slimOpen = false;
  }

  private void processWikiText(PageData pageData) throws Exception {
    runTestsOnPage(pageData);
    String wikiText = generateWikiTextForTestResults();
    pageData.setContent(wikiText);
    testResults = pageData;
  }

  private void runTestsOnPage(PageData pageData) throws Exception {
    tableScanner = new TableScanner(pageData);
    instructions = createInstructions();
    instructionResults = slimClient.invokeAndGetResponse(instructions);
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
    if (beginsWith(tableType, "DT:"))
      return new DecisionTable(table, tableId, this);
    else if (beginsWith(tableType, "Query:"))
      return new QueryTable(table, tableId, this);
    else if (beginsWith(tableType, "Table"))
      return new TableTable(table, tableId, this);
    else if (tableType.equalsIgnoreCase("script"))
      return new ScriptTable(table, tableId, this);
    else if (tableType.equalsIgnoreCase("import"))
      return new ImportTable(table, tableId);
    else
      return new SlimErrorTable(table, tableId);
  }

  private boolean beginsWith(String tableType, String typeCode) {
    return tableType.toUpperCase().startsWith(typeCode.toUpperCase());
  }

  private String generateWikiTextForTestResults() throws Exception {
    replaceExceptionsWithLinks();
    putTestResultsIntoTables();
    return ExceptionList.toWikiText(exceptions) + testResultsToWikiText();
  }

  private void replaceExceptionsWithLinks() {
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
      "!*> Standard Output\n\n" +
      (fastTest ? "\n" : slimRunner.getOutput()) +
      "*!\n" +
      "!*> Standard Error\n\n" +
      (fastTest ? "\n" : slimRunner.getError()) +
      "*!\n" +
      "!*> Command Line\n{{{" +
      slimCommand + "}}}\n" +
      "*!\n";

    return wikiText;
  }

  private void putTestResultsIntoTables() {
    for (SlimTable table : testTables) {
      try {
        table.evaluateExpectations(instructionResults);
      } catch (Throwable e) {
        exceptions.put("ABORT", exceptionToString(e));
      }
    }
  }

  //For testing only.  Makes responder faster.
  private void createSlimService(String args) throws Exception {
    while (!tryCreateSlimService(args))
      Thread.sleep(10);
  }

  private boolean tryCreateSlimService(String args) throws Exception {
    try {
      SlimService.main(args.trim().split(" "));
      return true;
    } catch (Exception e) {
      return false;
    }
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

  public String getSymbol(String symbolName) {
    return symbols.get(symbolName);
  }

  public void setSymbol(String symbolName, String value) {
    symbols.put(symbolName, value);
  }

  protected boolean isFastTest() {
    return fastTest;
  }

  protected void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
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

  public static String exceptionToString(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    e.printStackTrace(pw);
    return SlimServer.EXCEPTION_TAG + stringWriter.toString();
  }
}

