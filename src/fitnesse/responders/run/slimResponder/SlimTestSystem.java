// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.components.CommandRunner;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.slimTables.*;
import fitnesse.testutil.MockCommandRunner;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.Symbol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SlimTestSystem extends TestSystem implements SlimTestContext {
  public static final String MESSAGE_ERROR = "!error:";
  public static final String MESSAGE_FAIL = "!fail:";
  public static final SlimTable START_OF_TEST = null;
  public static final SlimTable END_OF_TEST = null;

  private CommandRunner slimRunner;
  private String slimCommand;
  private SlimClient slimClient;

  protected Map<String, Object> allInstructionResults = new HashMap<String, Object>();
  protected List<SlimTable> allTables = new ArrayList<SlimTable>();
  protected List<Object> allInstructions = new ArrayList<Object>();
  protected List<SlimTable.Expectation> allExpectations = new ArrayList<SlimTable.Expectation>();


  protected List<Object> instructions;
  private boolean started;
  protected PageData testResults;
  protected TableScanner tableScanner;
  protected Map<String, Object> instructionResults;
  protected List<SlimTable> testTables = new ArrayList<SlimTable>();
  protected ExceptionList exceptions = new ExceptionList();
  private Map<String, String> symbols = new HashMap<String, String>();
  protected TestSummary testSummary;
  private static AtomicInteger slimSocketOffset = new AtomicInteger(0);
  private int slimSocket;
  protected final Pattern exceptionMessagePattern = Pattern.compile("message:<<(.*)>>");
  private Map<String, ScenarioTable> scenarios = new HashMap<String, ScenarioTable>();
  protected List<SlimTable.Expectation> expectations = new ArrayList<SlimTable.Expectation>();
  private SlimTableFactory slimTableFactory = new SlimTableFactory();
  private Symbol preparsedScenarioLibrary;


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
    }
    else if (manualStart) {
      slimSocket = getSlimPortBase();
      slimRunner = new MockCommandRunner();
    } else {
      slimRunner = new CommandRunner(slimCommand, "", createClasspathEnvironment(classPath));
    }
    return new ExecutionLog(page, slimRunner);
  }

  public int findFreePort() {
    int port;
    try {
      ServerSocket socket = new ServerSocket(0);
      port = socket.getLocalPort();
      socket.close();
    } catch (Exception e) {
      port = -1;
    }
    return port;
  }

  public int getNextSlimSocket() {
    int base = getSlimPortBase();
    if (base == 0) {
      return findFreePort();
    }
    synchronized (slimSocketOffset) {
      int offset = slimSocketOffset.get();
      offset = (offset + 1) % 10;
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

    slimClient = new SlimClient(determineSlimHost(), slimSocket);
    try {
      waitForConnection();
      started = true;
    } catch (SlimError e) {
      testSystemListener.exceptionOccurred(e);
    }
  }

  String determineSlimHost() throws Exception {
    String slimHost = page.getData().getVariable("SLIM_HOST");
    return slimHost == null ? "localhost" : slimHost;
  }

  public String getCommandLine() {
    return slimCommand;
  }

  public void bye() throws Exception {
    slimClient.sendBye();
    if (!fastTest && !manualStart) {
      slimRunner.join();
    }
    if (fastTest) {
      slimRunner.kill();
    }
  }

  //For testing only.  Makes responder faster.
  void createSlimService(String args) throws Exception {
    while (!tryCreateSlimService(args))
      Thread.sleep(10);
  }

  private boolean tryCreateSlimService(String args) throws SocketException {
    try {
      SlimService.main(args.trim().split(" "));
      return true;
    } catch (SocketException e) {
      throw e;
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
    initializeTest();
    checkForAndReportVersionMismatch(pageData);
    String html = processAllTablesOnPage(pageData);
    testComplete(testSummary);
    return html;
  }

  private void initializeTest() {
    symbols.clear();
    scenarios.clear();
    testSummary.clear();
    allExpectations.clear();
    allInstructionResults.clear();
    allInstructions.clear();
    allTables.clear();
    exceptions.resetForNewTest();
  }

  private void checkForAndReportVersionMismatch(PageData pageData) throws Exception {
    double expectedVersionNumber = getExpectedSlimVersion(pageData);
    double serverVersionNumber = slimClient.getServerVersion();
    if (serverVersionNumber < expectedVersionNumber)
      exceptions.addException("Slim Protocol Version Error",
        String.format("Expected V%s but was V%s", expectedVersionNumber, serverVersionNumber));
  }

  private double getExpectedSlimVersion(PageData pageData) throws Exception {
    double expectedVersionNumber = SlimClient.MINIMUM_REQUIRED_SLIM_VERSION;
    String pageSpecificSlimVersion = pageData.getVariable("SLIM_VERSION");
    if (pageSpecificSlimVersion != null) {
      try {
        double pageSpecificSlimVersionDouble = Double.parseDouble(pageSpecificSlimVersion);
        expectedVersionNumber = pageSpecificSlimVersionDouble;
      } catch (NumberFormatException e) {
      }
    }
    return expectedVersionNumber;
  }

  protected abstract String createHtmlResults(SlimTable startAfterTable, SlimTable lastWrittenTable) throws Exception;

  String processAllTablesOnPage(PageData pageData) throws Exception {
    tableScanner = scanTheTables(pageData);
    allTables = createSlimTables(tableScanner);
    testResults = pageData;

    boolean runAllTablesAtOnce = false;
    String htmlResults = "";
    if (runAllTablesAtOnce || (allTables.size() == 0)) {
      htmlResults = processTablesAndGetHtml(allTables, START_OF_TEST, END_OF_TEST);
    } else {
      List<SlimTable> oneTableList = new ArrayList<SlimTable>(1);
      for (int index = 0; index < allTables.size(); index++) {
        SlimTable theTable = allTables.get(index);
        SlimTable startWithTable = (index == 0) ? START_OF_TEST : theTable;
        SlimTable nextTable = (index + 1 < allTables.size()) ? allTables.get(index + 1) : END_OF_TEST;

        oneTableList.add(theTable);
        htmlResults += processTablesAndGetHtml(oneTableList, startWithTable, nextTable);
        oneTableList.clear();
      }
    }
    return htmlResults;
  }

  protected abstract TableScanner scanTheTables(PageData pageData) throws Exception;

  private String processTablesAndGetHtml(List<SlimTable> tables, SlimTable startWithTable, SlimTable nextTable) throws Exception {
    expectations.clear();

    testTables = tables;
    instructions = createInstructions(tables);
    if (!exceptions.stopTestCalled()) {
      instructionResults = slimClient.invokeAndGetResponse(instructions);
    }
    String html = createHtmlResults(startWithTable, nextTable);
    acceptOutputFirst(html);

    // update all lists
    allExpectations.addAll(expectations);
    allInstructions.addAll(instructions);
    allInstructionResults.putAll(instructionResults);

    return html;
  }

  private List<Object> createInstructions(List<SlimTable> tables) {
    List<Object> instructions = new ArrayList<Object>();
    for (SlimTable table : tables) {
      table.appendInstructions(instructions);
    }
    return instructions;
  }

  private List<SlimTable> createSlimTables(TableScanner tableScanner) {
    List<SlimTable> allTables = new LinkedList<SlimTable>();
    for (Table table : tableScanner) {
      String tableId = "" + allTables.size();
      SlimTable slimTable = slimTableFactory.makeSlimTable(table, tableId, this);
      if (slimTable != null) {
        allTables.add(slimTable);
      }
    }
    return allTables;
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
        exceptions.addException("ABORT", exceptionToString(ex));
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
      exceptions.addException("ABORT", exceptionToString(e));
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
        processException(resultKey, resultString);
    }
  }

  private boolean shouldReportException(String resultKey, String resultString) {
    for (SlimTable table : testTables) {
      if (table.shouldIgnoreException(resultKey, resultString))
        return false;
    }
    return true;
  }

  private void processException(String resultKey, String resultString) {
    testSummary.exceptions++;
    boolean isStopTestException = resultString.contains(SlimServer.EXCEPTION_STOP_TEST_TAG);
    if (isStopTestException) {
      exceptions.setStopTestCalled();
    }

    Matcher exceptionMessageMatcher = exceptionMessagePattern.matcher(resultString);
    if (exceptionMessageMatcher.find()) {
      String prefix = (isStopTestException) ? MESSAGE_FAIL : MESSAGE_ERROR;
      String exceptionMessage = exceptionMessageMatcher.group(1);
      instructionResults.put(resultKey, prefix + translateExceptionMessage(exceptionMessage));
    } else {
      exceptions.addException(resultKey, resultString);
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
    return allTables;
  }

  public List<Object> getInstructions() {
    return allInstructions;
  }

  public Map<String, Object> getInstructionResults() {
    return allInstructionResults;
  }

  public List<SlimTable.Expectation> getExpectations() {
    return allExpectations;
  }


  public Symbol getPreparsedScenarioLibrary() throws Exception {
    if (preparsedScenarioLibrary == null) {
      preparsedScenarioLibrary = Parser.make(page, getScenarioLibraryContent()).parse();
    }
    return preparsedScenarioLibrary;
  }

  private String getScenarioLibraryContent() throws Exception {
    String content = "!*> Precompiled Libraries\n\n";
    content += includeUncleLibraries();
    content += "*!\n";
    return content;
  }

  private String includeUncleLibraries() throws Exception {
    String content = "";
    List<WikiPage> uncles = PageCrawlerImpl.getAllUncles("ScenarioLibrary", page);
    Collections.reverse(uncles);
    for (WikiPage uncle : uncles)
      content += include(page.getPageCrawler().getFullPath(uncle));
    return content;
  }

  private String include(WikiPagePath path) {
    return "!include -c ." + path + "\n";
  }
}
