// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.components.CommandRunner;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.slimTables.DecisionTable;
import fitnesse.slimTables.ImportTable;
import fitnesse.slimTables.OrderedQueryTable;
import fitnesse.slimTables.QueryTable;
import fitnesse.slimTables.ScenarioTable;
import fitnesse.slimTables.ScriptTable;
import fitnesse.slimTables.SlimErrorTable;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.Table;
import fitnesse.slimTables.TableScanner;
import fitnesse.slimTables.TableTable;
import fitnesse.testutil.MockCommandRunner;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public abstract class SlimTestSystem extends TestSystem implements SlimTestContext {
  private CommandRunner slimRunner;
  private String slimCommand;
  private SlimClient slimClient;
  protected List<Object> instructions;
  private boolean started;
  protected TableScanner tableScanner;
  protected PageData testResults;
  protected Map<String, Object> instructionResults;
  protected List<SlimTable> testTables = new ArrayList<SlimTable>();
  protected Map<String, String> exceptions = new HashMap<String, String>();
  private Map<String, String> symbols = new HashMap<String, String>();
  protected TestSummary testSummary;
  private static AtomicInteger slimSocketOffset = new AtomicInteger(0);
  private int slimSocket;
  protected final Pattern exceptionMessagePattern = Pattern.compile("message:<<(.*)>>");
  private Map<String, ScenarioTable> scenarios = new HashMap<String, ScenarioTable>();
  protected List<SlimTable.Expectation> expectations = new ArrayList<SlimTable.Expectation>();

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

  public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
    scenarios.put(scenarioName, scenarioTable);
  }

  public ScenarioTable getScenario(String scenarioName) {
    return scenarios.get(scenarioName);
  }

  public void addExpectation(SlimTable.Expectation e) {
    expectations.add(e);
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

  protected ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) throws Exception {
    String slimFlags = getSlimFlags();
    slimSocket = getNextSlimSocket();
    String slimArguments = String.format("%s %d", slimFlags, slimSocket);
    String slimCommandPrefix = buildCommand(descriptor, classPath);
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
    int base = getSlimPortBase();
    synchronized (slimSocketOffset) {
      int offset = slimSocketOffset.get();
      offset = (offset+1)%10;
      slimSocketOffset.set(offset);
      return offset + base;
    }
  }

  private int getSlimPortBase() {
    int base = 8085;
    try {
      String slimPort = page.getData().getVariable("SLIM_PORT");
      if (slimPort != null) {
        int slimPortInt = Integer.parseInt(slimPort);
        base = slimPortInt;
      }
    } catch (Exception e) {
    }
    return base;
  }

  public void start() throws Exception {
    slimRunner.asynchronousStart();
    slimClient = new SlimClient("localhost", slimSocket);
    try {
      waitForConnection();
      started = true;
    } catch (SlimError e) {
      testSystemListener.exceptionOccurred(e);
    }
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

  public String runTestsAndGenerateHtml(PageData pageData) throws Exception {
    testTables.clear();
    expectations.clear();
    symbols.clear();
    scenarios.clear();
    exceptions.clear();
    testSummary.clear();
    runTestsOnPage(pageData);
    testResults = pageData;
    String html = createHtmlResults();
    acceptOutputFirst(html);
    acceptResultsLast(testSummary);
    return html;
  }

  protected abstract String createHtmlResults() throws Exception;

  void runTestsOnPage(PageData pageData) throws Exception {
    tableScanner = scanTheTables(pageData);
    instructions = createInstructions(this);
    instructionResults = slimClient.invokeAndGetResponse(instructions);
  }

  protected abstract TableScanner scanTheTables(PageData pageData) throws Exception;

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
    else if (beginsWith(tableType, "ordered query:"))
      return new OrderedQueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "query:"))
      return new QueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "table"))
      return new TableTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("script"))
      return new ScriptTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("scenario"))
      return new ScenarioTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("comment"))
      return null;
    else if (tableType.equalsIgnoreCase("import"))
      return new ImportTable(table, tableId, slimTestContext);
    else if (doesNotHaveColon(tableType))
      return new DecisionTable(table, tableId, slimTestContext);
    else
      return new SlimErrorTable(table, tableId, slimTestContext);
  }

  private boolean doesNotHaveColon(String tableType) {
    return tableType.indexOf(":") == -1;
  }

  private boolean beginsWith(String tableType, String typeCode) {
    return tableType.toUpperCase().startsWith(typeCode.toUpperCase());
  }

  static String translateExceptionMessage(String exceptionMessage) {
    String tokens[] = exceptionMessage.split(" ");
    if (tokens[0].equals("COULD_NOT_INVOKE_CONSTRUCTOR"))
      return "Could not invoke constructor for " + tokens[1];
    else if (tokens[0].equals("NO_METHOD_IN_CLASS"))
      return String.format("Method %s not found in %s", tokens[1], tokens[2]);
    else if (tokens[0].equals("NO_CONSTRUCTOR"))
      return String.format("Could not find constructor for %s", tokens[1]);
    else if (tokens[0].equals("NO_CONVERTER_FOR_ARGUMENT_NUMBER"))
      return String.format("No converter for %s", tokens[1]);
    else if (tokens[0].equals("NO_INSTANCE"))
      return String.format("The instance %s does not exist", tokens[1]);
    else if (tokens[0].equals("NO_CLASS"))
      return String.format("Could not find class %s", tokens[1]);
    else if (tokens[0].equals("MALFORMED_INSTRUCTION"))
      return String.format("The instruction %s is malformed", exceptionMessage.substring(exceptionMessage.indexOf(" ") + 1));

    return exceptionMessage;
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

  public TestSummary getTestSummary() {
    return testSummary;
  }

  protected void evaluateExpectations() {
    for (SlimTable.Expectation e : expectations) {
      try {
        e.evaluateExpectation(instructionResults);
      } catch (Throwable ex) {
        exceptions.put("ABORT", exceptionToString(ex));
        exceptionOccurred(ex);
      }
    }
  }

  protected void evaluateTables() {
    evaluateExpectations();
    for (SlimTable table : testTables)
      evaluateTable(table);
  }

  private void evaluateTable(SlimTable table) {
    try {
      table.evaluateReturnValues(instructionResults);
      testSummary.add(table.getTestSummary());
    } catch (Throwable e) {
      exceptions.put("ABORT", exceptionToString(e));
      exceptionOccurred(e);
    }
  }

  protected void replaceExceptionsWithLinks() {
    Set<String> resultKeys = instructionResults.keySet();
    for (String resultKey : resultKeys)
      replaceExceptionWithExceptionLink(resultKey);
  }

  private void replaceExceptionWithExceptionLink(String resultKey) {
    Object result = instructionResults.get(resultKey);
    if (result instanceof String)
      replaceIfUnignoredException(resultKey, (String) result);
  }

  private void replaceIfUnignoredException(String resultKey, String resultString) {
    if (resultString.indexOf(SlimServer.EXCEPTION_TAG) != -1) {
      if (shouldReportException(resultKey, resultString))
        replaceException(resultKey, resultString);
    }
  }

  private boolean shouldReportException(String resultKey, String resultString) {
    for (SlimTable table : testTables) {
      if (table.shouldIgnoreException(resultKey, resultString))
        return false;
    }
    return true;
  }

  private void replaceException(String resultKey, String resultString) {
    testSummary.exceptions++;
    Matcher exceptionMessageMatcher = exceptionMessagePattern.matcher(resultString);
    if (exceptionMessageMatcher.find()) {
      String exceptionMessage = exceptionMessageMatcher.group(1);
      instructionResults.put(resultKey, "!:" + translateExceptionMessage(exceptionMessage));
    } else {
      exceptions.put(resultKey, resultString);
      instructionResults.put(resultKey, exceptionResult(resultKey));
    }
  }

  private String exceptionResult(String resultKey) {
    return String.format("Exception: <a href=#%s>%s</a>", resultKey, resultKey);
  }

  public Map<String, ScenarioTable> getScenarios() {
    return scenarios;
  }

  public static void clearSlimPortOffset() {
    slimSocketOffset.set(0);
  }

  public List<SlimTable> getTestTables() {
    return testTables;
  }

  public List<Object> getInstructions() {
    return instructions;
  }

  public Map<String, Object> getInstructionResults() {
    return instructionResults;
  }

  public List<SlimTable.Expectation> getExpectations() {
    return expectations;
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

    private String toHtml() {
      header();
      exceptions();
      footer();
      return buffer.toString();
    }

    private void footer() {
      if (keys.size() > 0)
        buffer.append("<hr/>");
    }

    private void exceptions() {
      for (String key : keys) {
        buffer.append(String.format("<a name=\"%s\"/><b></b>", key));
        String collapsibleSectionFormat = "<div class=\"collapse_rim\">" +
          "<div style=\"float: right;\" class=\"meta\"><a href=\"javascript:expandAll();\">Expand All</a> | <a href=\"javascript:collapseAll();\">Collapse All</a></div>" +
          "<a href=\"javascript:toggleCollapsable('%d');\">" +
          "<img src=\"/files/images/collapsableClosed.gif\" class=\"left\" id=\"img%d\"/>" +
          "</a>" +
          "&nbsp;<span class=\"meta\">%s </span>\n" +
          "\n" +
          "\t<div class=\"hidden\" id=\"%d\"><pre>%s</pre></div>\n" +
          "</div>";
        long id = new Random().nextLong();
        buffer.append(String.format(collapsibleSectionFormat, id, id, key, id, exceptions.get(key)));
      }
    }

    private void header() {
      if (keys.size() > 0) {
        buffer.append("<H3> <span class=\"fail\">Exceptions</span></H3><br/>");
      }
    }

    protected static String toHtml(Map<String, String> exceptions) {
      return new ExceptionList(exceptions).toHtml();
    }
  }
}
