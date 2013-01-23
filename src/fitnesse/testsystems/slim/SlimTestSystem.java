// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.components.CommandRunner;
import fitnesse.responders.PageFactory;
import fitnesse.slim.*;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.FailResult;
import fitnesse.testsystems.slim.tables.*;
import fitnesse.testutil.MockCommandRunner;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static fitnesse.slim.SlimServer.*;

public abstract class SlimTestSystem extends TestSystem {
  public static final String MESSAGE_ERROR = "!error:";
  public static final String MESSAGE_FAIL = "!fail:";
  public static final SlimTable START_OF_TEST = null;
  public static final SlimTable END_OF_TEST = null;

  private CommandRunner slimRunner;
  private SlimClient slimClient;

  // TODO: get rid of those. Expose via Listener interface
  protected Map<String, Object> allInstructionResults = new HashMap<String, Object>();
  protected List<SlimTable> allTables = new ArrayList<SlimTable>();
  protected List<Assertion> allAssertions = new ArrayList<Assertion>();


  private boolean started;
  protected ReadOnlyPageData testResults;
  protected TableScanner tableScanner;
  protected Map<String, Object> instructionResults;
  protected List<SlimTable> testTables = new ArrayList<SlimTable>();
  protected ExceptionList exceptions = new ExceptionList();
  protected TestSummary testSummary;
  private SlimTableFactory slimTableFactory = new SlimTableFactory();
  private NestedSlimTestContext testContext;
  private final SlimDescriptor descriptor;
  private List<Assertion> assertions;


  public SlimTestSystem(WikiPage page, Descriptor descriptor, TestSystemListener listener) {
    super(page, listener);
    this.descriptor = new SlimDescriptor(descriptor);
    testSummary = new TestSummary(0, 0, 0, 0);
  }

  public SlimTestContext getTestContext() {
    return testContext;
  }

  public boolean isSuccessfullyStarted() {
    return started;
  }

  public void kill() throws IOException {
    if (slimRunner != null)
      slimRunner.kill();
    if (slimClient != null)
      slimClient.close();
  }

  protected ExecutionLog createExecutionLog() throws SocketException {
    final String classPath = descriptor.getClassPath();
    final String slimArguments = buildArguments();
    if (fastTest) {
      slimRunner = new MockCommandRunner();
      createSlimService(slimArguments);
    }
    else if (manualStart) {
      slimRunner = new MockCommandRunner();
    } else {
      slimRunner = new CommandRunner(buildCommand(), "", createClasspathEnvironment(classPath));
    }
    return new ExecutionLog(page, slimRunner);
  }

  public String buildCommand() {
    String slimArguments = buildArguments();
    String slimCommandPrefix = super.buildCommand(descriptor);
    return String.format("%s %s", slimCommandPrefix, slimArguments);
  }

  private String buildArguments() {
    int slimSocket = descriptor.getSlimPort();
    String slimFlags = descriptor.getSlimFlags();
    return String.format("%s %d", slimFlags, slimSocket);
  }

  public void start() throws IOException {
    slimRunner.asynchronousStart();

    slimClient = new SlimClient(descriptor.determineSlimHost(), descriptor.getSlimPort());
    try {
      waitForConnection();
      started = true;
    } catch (SlimError e) {
      testSystemListener.exceptionOccurred(e);
    }
  }

  public void bye() throws IOException {
    slimClient.sendBye();
    if (!fastTest && !manualStart) {
      slimRunner.join();
    }
    if (fastTest) {
      slimRunner.kill();
    }
  }

  //For testing only.  Makes responder faster.
  void createSlimService(String args) throws SocketException {
    while (!tryCreateSlimService(args))
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
  }

  private boolean tryCreateSlimService(String args) throws SocketException {
    try {
      SlimService.parseCommandLine(args.trim().split(" "));
      SlimService.startWithFactoryAsync(new JavaSlimFactory());
      return true;
    } catch (SocketException e) {
      throw e;
    } catch (Exception e) {
      return false;
    }
  }

  void waitForConnection() {
    while (!isConnected())
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
  }

  private boolean isConnected() {
    try {
      slimClient.connect();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String runTestsAndGenerateHtml(ReadOnlyPageData pageData) throws IOException {
    initializeTest();
    checkForAndReportVersionMismatch(pageData);
    String html = processAllTablesOnPage(pageData);
    testComplete(testSummary);
    return html;
  }

  private void initializeTest() {
    testContext = new NestedSlimTestContext();
    testSummary.clear();
    allInstructionResults.clear();
    allAssertions.clear();
    allTables.clear();
    exceptions.resetForNewTest();
  }

  private void checkForAndReportVersionMismatch(ReadOnlyPageData pageData) {
    double expectedVersionNumber = getExpectedSlimVersion(pageData);
    double serverVersionNumber = slimClient.getServerVersion();
    if (serverVersionNumber == SlimClient.NO_SLIM_SERVER_CONNECTION_FLAG) {
    	exceptions.addException("Sever Not Connected Error", "Server did not respond with a valid version number.");
    }
    else {
    	if (serverVersionNumber < expectedVersionNumber)
      exceptions.addException("Slim Protocol Version Error",
        String.format("Expected V%s but was V%s", expectedVersionNumber, serverVersionNumber));
    }
  }

  private double getExpectedSlimVersion(ReadOnlyPageData pageData) {
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

  protected abstract String createHtmlResults(SlimTable startAfterTable, SlimTable lastWrittenTable);

  String processAllTablesOnPage(ReadOnlyPageData pageData) throws IOException {
    tableScanner = scanTheTables(pageData);
    allTables = createSlimTables(tableScanner);
    testResults = pageData;

    boolean runAllTablesAtOnce = false;
    StringBuilder htmlResults = new StringBuilder();
    if (runAllTablesAtOnce || (allTables.size() == 0)) {
      htmlResults.append(processTablesAndGetHtml(allTables, START_OF_TEST, END_OF_TEST));
    } else {
      List<SlimTable> oneTableList = new ArrayList<SlimTable>(1);
      for (int index = 0; index < allTables.size(); index++) {
        SlimTable theTable = allTables.get(index);
        SlimTable startWithTable = (index == 0) ? START_OF_TEST : theTable;
        SlimTable nextTable = (index + 1 < allTables.size()) ? allTables.get(index + 1) : END_OF_TEST;

        oneTableList.add(theTable);
        htmlResults.append(processTablesAndGetHtml(oneTableList, startWithTable, nextTable));
        oneTableList.clear();
      }
    }
    return htmlResults.toString();
  }

  protected abstract TableScanner scanTheTables(ReadOnlyPageData pageData);

  private String processTablesAndGetHtml(List<SlimTable> tables, SlimTable startWithTable, SlimTable nextTable) throws IOException {

    testTables = tables;
    assertions = createAssertions(tables);
    if (!exceptions.stopTestCalled()) {
      instructionResults = slimClient.invokeAndGetResponse(Assertion.getInstructions(assertions));
    }
    String html = createHtmlResults(startWithTable, nextTable);
    acceptOutputFirst(html);

    // update all lists
    allAssertions.addAll(assertions);
    allInstructionResults.putAll(instructionResults);

    return html;
  }


  private List<Assertion> createAssertions(List<SlimTable> tables) {
    List<Assertion> assertions = new ArrayList<Assertion>();
    for (SlimTable table : tables) {
      try {
        assertions.addAll(table.getAssertions());
      } catch (SyntaxError e) {
        String tableName = table.getTable().getCellContents(0, 0);
        // TODO: remove: raise TableFormatException or something like that.
        table.getTable().setCell(0, 0, new FailResult(String.format("%s: <strong>Bad table! %s</strong>", tableName, e.getMessage())));
      }
    }
    return assertions;
  }

  private List<SlimTable> createSlimTables(TableScanner tableScanner) {
    List<SlimTable> allTables = new LinkedList<SlimTable>();
    for (Table table : tableScanner)
      createSlimTable(allTables, table);

    return allTables;
  }

  private void createSlimTable(List<SlimTable> allTables, Table table) {
    String tableId = "" + allTables.size();
    SlimTable slimTable = slimTableFactory.makeSlimTable(table, tableId, testContext);
    if (slimTable != null) {
      allTables.add(slimTable);
    }
  }

  static String translateExceptionMessage(String exceptionMessage) {
    String tokens[] = exceptionMessage.split(" ");
    if (tokens[0].equals(COULD_NOT_INVOKE_CONSTRUCTOR))
      return "Could not invoke constructor for " + tokens[1];
    else if (tokens[0].equals(NO_METHOD_IN_CLASS))
      return String.format("Method %s not found in %s", tokens[1], tokens[2]);
    else if (tokens[0].equals(NO_CONSTRUCTOR))
      return String.format("Could not find constructor for %s", tokens[1]);
    else if (tokens[0].equals(NO_CONVERTER_FOR_ARGUMENT_NUMBER))
      return String.format("No converter for %s", tokens[1]);
    else if (tokens[0].equals(NO_INSTANCE))
      return String.format("The instance %s does not exist", tokens[1]);
    else if (tokens[0].equals(NO_CLASS))
      return String.format("Could not find class %s", tokens[1]);
    else if (tokens[0].equals(MALFORMED_INSTRUCTION))
      return String.format("The instruction %s is malformed", exceptionMessage.substring(exceptionMessage.indexOf(" ") + 1));

    return exceptionMessage;
  }

  public ReadOnlyPageData getTestResults() {
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

  protected void evaluateTables() {
    for (Assertion a : assertions) {
      try {
        Object returnValue = instructionResults.get(a.getInstruction().getId());
        if (returnValue != null && returnValue instanceof String && ((String)returnValue).contains(EXCEPTION_TAG)) {
          String key = a.getInstruction().getId();
          if (shouldReportException(key, (String) returnValue)) {
            returnValue = processException(key, (String) returnValue);
          }
        }
        a.getExpectation().evaluateExpectation(returnValue);
      } catch (Throwable ex) {
        exceptions.addException("ABORT", exceptionToString(ex));
        exceptionOccurred(ex);
      }
    }
  }

  private boolean shouldReportException(String resultKey, String resultString) {
    for (SlimTable table : testTables) {
      if (table.shouldIgnoreException(resultKey, resultString))
        return false;
    }
    return true;
  }

  private ExceptionResult processException(String resultKey, String resultString) {
    testSummary.exceptions++;
    boolean isStopTestException = resultString.contains(EXCEPTION_STOP_TEST_TAG);
    if (isStopTestException) {
      exceptions.setStopTestCalled();
    }

    return new ExceptionResult(resultKey, resultString);
  }

  public List<SlimTable> getTestTables() {
    return allTables;
  }

  public List<Assertion> getAssertions() {
    return allAssertions;
  }

  public Map<String, Object> getInstructionResults() {
    return allInstructionResults;
  }

  public static class SlimDescriptor extends Descriptor {

    private static AtomicInteger slimPortOffset = new AtomicInteger(0);
    private final int slimPort;

    public SlimDescriptor(WikiPage page, PageFactory pageFactory, boolean remoteDebug) {
      super(page, pageFactory, remoteDebug);
      slimPort = getNextSlimPort();
    }

    public SlimDescriptor(Descriptor descriptor) {
      super(descriptor);
      slimPort = getNextSlimPort();
    }

    public int getSlimPort() {
      return slimPort;
    }

    private int findFreePort() {
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

    private int getNextSlimPort() {
      int base;

      if (System.getProperty("slim.port") != null) {
        base = Integer.parseInt(System.getProperty("slim.port"));
      } else {
        base = getSlimPortBase();
      }

      if (base == 0) {
        return findFreePort();
      }

      synchronized (slimPortOffset) {
        int offset = slimPortOffset.get();
        offset = (offset + 1) % 10;
        slimPortOffset.set(offset);
        return offset + base;
      }
    }

    public static void clearSlimPortOffset() {
      slimPortOffset.set(0);
    }

    private int getSlimPortBase() {
      int base = 8085;
      try {
        String slimPort = getPageData().getVariable("SLIM_PORT");
        if (slimPort != null) {
          int slimPortInt = Integer.parseInt(slimPort);
          base = slimPortInt;
        }
      } catch (Exception e) {
      }
      return base;
    }

    String determineSlimHost() {
      String slimHost = getPageData().getVariable("SLIM_HOST");
      return slimHost == null ? "localhost" : slimHost;
    }

    String getSlimFlags() {
      String slimFlags = getPageData().getVariable("SLIM_FLAGS");
      if (slimFlags == null)
        slimFlags = "";
      return slimFlags;
    }


  }

  private class NestedSlimTestContext implements SlimTestContext {
    private Map<String, String> symbols = new HashMap<String, String>();
    private Map<String, ScenarioTable> scenarios = new HashMap<String, ScenarioTable>();

    @Override
    public String getSymbol(String symbolName) {
      return symbols.get(symbolName);
    }

    @Override
    public void setSymbol(String symbolName, String value) {
      symbols.put(symbolName, value);
    }

    @Override
    public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
      scenarios.put(scenarioName, scenarioTable);
    }

    @Override
    public ScenarioTable getScenario(String scenarioName) {
      return scenarios.get(scenarioName);
    }

    @Override
    public Collection<ScenarioTable> getScenarios() {
      return scenarios.values();
    }

    @Override
    public void incrementPassedTestsCount() {
      testSummary.right++;
    }

    @Override
    public void incrementFailedTestsCount() {
      testSummary.wrong = testSummary.getWrong() + 1;
    }

    @Override
    public void incrementErroredTestsCount() {
      testSummary.exceptions = testSummary.getExceptions() + 1;
    }

    @Override
    public void incrementIgnoredTestsCount() {
      testSummary.ignores++;
    }
  }
}
