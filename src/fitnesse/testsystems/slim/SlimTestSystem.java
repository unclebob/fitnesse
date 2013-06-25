// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import static fitnesse.slim.SlimServer.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimCommandRunningClient;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.testsystems.slim.tables.SyntaxError;
import fitnesse.wiki.ReadOnlyPageData;

public abstract class SlimTestSystem implements TestSystem {
  public static final SlimTable START_OF_TEST = null;
  public static final SlimTable END_OF_TEST = null;

  private final SlimClient slimClient;
  private final TestSystemListener testSystemListener;
  private final ExecutionLog log;
  private final String testSystemName;

  private SlimTableFactory slimTableFactory = new SlimTableFactory();
  private SlimTestContextImpl testContext;
  private boolean stopTestCalled;


  public SlimTestSystem(String testSystemName, SlimClient slimClient, TestSystemListener listener, ExecutionLog executionLog) {
    this.testSystemName = testSystemName;
    this.slimClient = slimClient;
    this.testSystemListener = listener;
    this.log = executionLog;
  }

  @Override
  public ExecutionLog getExecutionLog() {
    return log;
  }

  public SlimTestContext getTestContext() {
    return testContext;
  }

  public boolean isSuccessfullyStarted() {
    return true;
  }


  public void start() throws IOException {
    try {
      slimClient.start();
      testSystemListener.testSystemStarted(this, testSystemName, slimClient.getTestRunner());
    } catch (SlimError e) {
      exceptionOccurred(e);
    }
  }

  public void kill() throws IOException {
    if (slimClient != null)
      slimClient.close();
  }

  public void bye() throws IOException {
    slimClient.sendBye();
  }

  @Override
  public void runTests(TestPage pageToTest) throws IOException {
    initializeTest();
    processAllTablesOnPage(pageToTest);
    testComplete(testContext.getTestSummary());
  }

  private void initializeTest() {
    testContext = new SlimTestContextImpl();
  }

  protected abstract List<SlimTable> createSlimTables(TestPage pageTotest);

  protected abstract String createHtmlResults(SlimTable startAfterTable, SlimTable lastWrittenTable);

  void processAllTablesOnPage(TestPage pageToTest) throws IOException {
    List<SlimTable> allTables = createSlimTables(pageToTest);

    if (allTables.size() == 0) {
      String html = createHtmlResults(START_OF_TEST, END_OF_TEST);
      testOutputChunk(html);
    } else {
      List<SlimTable> oneTableList = new ArrayList<SlimTable>(1);
      for (int index = 0; index < allTables.size(); index++) {
        SlimTable theTable = allTables.get(index);
        SlimTable startWithTable = (index == 0) ? START_OF_TEST : theTable;
        SlimTable nextTable = (index + 1 < allTables.size()) ? allTables.get(index + 1) : END_OF_TEST;

        processTable(theTable);

        String html = createHtmlResults(startWithTable, nextTable);
        testOutputChunk(html);
      }
    }
  }

  private void processTable(SlimTable table) throws IOException {
    List<Assertion> assertions = createAssertions(table);
    Map<String, Object> instructionResults;
    if (!stopTestCalled) {
      instructionResults = slimClient.invokeAndGetResponse(Assertion.getInstructions(assertions));
    } else {
      instructionResults = Collections.emptyMap();
    }

    evaluateTables(assertions, instructionResults);
  }

  private List<Assertion> createAssertions(SlimTable table) {
    List<Assertion> assertions = new ArrayList<Assertion>();
    try {
      assertions.addAll(table.getAssertions());
    } catch (SyntaxError e) {
      String tableName = table.getTable().getCellContents(0, 0);
      // TODO: remove: raise TableFormatException or something like that.
      table.getTable().updateContent(0, 0, TestResult.fail(String.format("%s: <strong>Bad table! %s</strong>", tableName, e.getMessage())));
    }
    return assertions;
  }

  protected List<SlimTable> createSlimTables(TableScanner<? extends Table> tableScanner) {
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

  public static String exceptionToString(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    e.printStackTrace(pw);
    return SlimServer.EXCEPTION_TAG + stringWriter.toString();
  }

  protected void evaluateTables(List<Assertion> assertions, Map<String, Object> instructionResults) {
    for (Assertion a : assertions) {
      try {
        final String key = a.getInstruction().getId();
        final Object returnValue = instructionResults.get(key);
        if (returnValue != null && returnValue instanceof String && ((String)returnValue).startsWith(EXCEPTION_TAG)) {
          ExceptionResult exceptionResult = makeExceptionResult(key, (String) returnValue);
          if (exceptionResult.isStopTestException()) {
            stopTestCalled = true;
          }
          exceptionResult = a.getExpectation().evaluateException(exceptionResult);
          if (exceptionResult != null) {
            testExceptionOccurred(a, exceptionResult);
          }
        } else {
          TestResult testResult = a.getExpectation().evaluateExpectation(returnValue);
          testAssertionVerified(a, testResult);
        }
      } catch (Throwable ex) {
        exceptionOccurred(ex);
      }
    }
  }

  private ExceptionResult makeExceptionResult(String resultKey, String resultString) {
    ExceptionResult exceptionResult = new ExceptionResult(resultKey, resultString);
    return exceptionResult;
  }

  public void testOutputChunk(String output) throws IOException {
    testSystemListener.testOutputChunk(output);
  }

  public void testComplete(TestSummary testSummary) throws IOException {
    testSystemListener.testComplete(testSummary);
  }

  public void exceptionOccurred(Throwable e) {
    log.addException(e);
    testSystemListener.exceptionOccurred(e);
  }

  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    testSystemListener.testAssertionVerified(assertion, testResult);
  }

  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    testSystemListener.testExceptionOccurred(assertion, exceptionResult);
  }

}
