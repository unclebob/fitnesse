package fitnesse.responders.run.slimResponder;

import fitnesse.components.CommandRunner;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.testutil.MockCommandRunner;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SlimTestSystem extends TestSystem implements SlimTestContext {
  private CommandRunner slimRunner;
  private String slimCommand;
  private SlimClient slimClient;
  private List<Object> instructions;
  private boolean started;
  private TableScanner tableScanner;
  private PageData testResults;
  private Map<String, Object> instructionResults;
  private List<SlimTable> testTables = new ArrayList<SlimTable>();
  private Map<String, String> exceptions = new HashMap<String, String>();
  private Map<String, String> symbols = new HashMap<String, String>();
  private TestSummary testSummary;
  private static AtomicInteger slimSocketOffset = new AtomicInteger(0);
  private int slimSocket;

  public SlimTestSystem(WikiPage page, TestSystemListener listener) {
    super(page, listener);
    testSummary = new TestSummary(0, 0, 0, 0);
  }

  public String getSymbol(String symbolName) {
    return symbols.get(symbolName);
  }

  public void setSymbol(String symbolName, String value) {
    symbols.put(symbolName, value);
  }

  public boolean isSuccessfullyStarted() {
    return started;
  }

  public void kill() throws Exception {
    if (slimRunner != null)
      slimRunner.kill();
    if (slimClient != null)
      slimClient.close();
  }

  String getSlimFlags() throws Exception {
    String slimFlags = page.getData().getVariable("SLIM_FLAGS");
    if (slimFlags == null)
      slimFlags = "";
    return slimFlags;
  }

  protected ExecutionLog createExecutionLog(String classPath, String className) throws Exception {
    String slimFlags = getSlimFlags();
    slimSocket = getNextSlimSocket();
    String slimArguments = String.format("%s %d", slimFlags, slimSocket);
    String slimCommandPrefix = buildCommand(className, classPath);
    slimCommand = String.format("%s %s", slimCommandPrefix, slimArguments);
    if (fastTest) {
      slimRunner = new MockCommandRunner();
      createSlimService(slimArguments);
    } else {
      slimRunner = new CommandRunner(slimCommand, "");
    }
    return new ExecutionLog(page, slimRunner);
  }

  public int getNextSlimSocket() {
    synchronized (slimSocketOffset) {
      int base = slimSocketOffset.get();
      base++;
      if (base >= 10)
        base = 0;
      slimSocketOffset.set(base);
      return base + 8085;
    }
  }

  public void start() throws Exception {
    slimRunner.start();
    slimClient = new SlimClient("localhost", slimSocket);
    waitForConnection();
    started = true;
  }

  public String getCommandLine() {
    return slimCommand;
  }

  public void bye() throws Exception {
    slimClient.sendBye();
    if (!fastTest)
      slimRunner.join();
  }

  //For testing only.  Makes responder faster.
  void createSlimService(String args) throws Exception {
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

  void waitForConnection() throws Exception {
    while (!isConnected())
      Thread.sleep(50);
  }

  private boolean isConnected() throws Exception {
    try {
      slimClient.connect();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  void runTestsOnPage(PageData pageData) throws Exception {
    tableScanner = new TableScanner(pageData);
    instructions = createInstructions(this);
    instructionResults = slimClient.invokeAndGetResponse(instructions);
  }

  private List<Object> createInstructions(SlimTestContext slimTestContext) {
    List<Object> instructions = new ArrayList<Object>();
    for (Table table : tableScanner) {
      String tableId = "" + testTables.size();
      SlimTable slimTable = makeSlimTable(table, tableId, slimTestContext);
      if (slimTable != null) {
        slimTable.appendInstructions(instructions);
        testTables.add(slimTable);
      }
    }
    return instructions;
  }

  private SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
    String tableType = table.getCellContents(0, 0);
    if (beginsWith(tableType, "dt:") || beginsWith(tableType, "decision:"))
      return new DecisionTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "query:"))
      return new QueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "table"))
      return new TableTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("script"))
      return new ScriptTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("comment"))
      return null;
    else if (tableType.equalsIgnoreCase("import"))
      return new ImportTable(table, tableId);
    else if (doesNotHaveColon(tableType))
      return new DecisionTable(table, tableId);
    else
      return new SlimErrorTable(table, tableId);
  }

  private boolean doesNotHaveColon(String tableType) {
    return tableType.indexOf(":") == -1;
  }

  private boolean beginsWith(String tableType, String typeCode) {
    return tableType.toUpperCase().startsWith(typeCode.toUpperCase());
  }

  public void sendPageData(PageData pageData) throws Exception {
    testTables.clear();
    symbols.clear();
    exceptions.clear();
    testSummary.clear();
    runTestsOnPage(pageData);
    String wikiText = generateWikiTextForTestResults();
    pageData.setContent(wikiText);
    testResults = pageData;
    acceptOutput(pageData.getHtml());
    acceptResults(testSummary);
  }

  private String generateWikiTextForTestResults() throws Exception {
    replaceExceptionsWithLinks();
    evaluateTables();
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
        testSummary.exceptions++;
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
      log.getCommandRunner().getOutput() +
      "*!\n" +
      "!*> Standard Error\n\n" +
      log.getCommandRunner().getError() +
      "*!\n" +
      "!*> Command Line\n{{{" +
      getCommandLine() + "}}}\n" +
      "*!\n";

    return wikiText;
  }

  private void evaluateTables() {
    for (SlimTable table : testTables)
      evaluateTable(table);
  }

  private void evaluateTable(SlimTable table) {
    try {
      table.evaluateExpectations(instructionResults);
      testSummary.add(table.getTestSummary());
    } catch (Throwable e) {
      exceptions.put("ABORT", exceptionToString(e));
      exceptionOccurred(e);
    }
  }

  public PageData getTestResults() {
    return testResults;
  }

  public static String exceptionToString(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    e.printStackTrace(pw);
    return SlimServer.EXCEPTION_TAG + stringWriter.toString();
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
