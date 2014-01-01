// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.CompositeTestSystemListener;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SyntaxError;

import static fitnesse.slim.SlimServer.*;

public abstract class SlimTestSystem implements TestSystem {
  private static final Logger LOG = Logger.getLogger(SlimTestSystem.class.getName());

  public static final SlimTable START_OF_TEST = null;
  public static final SlimTable END_OF_TEST = null;

  private final SlimClient slimClient;
  private final CompositeTestSystemListener testSystemListener;
  private final String testSystemName;

  private SlimTestContextImpl testContext;
  private boolean stopTestCalled;
  private boolean testSystemIsStopped;


  public SlimTestSystem(String testSystemName, SlimClient slimClient) {
    this.testSystemName = testSystemName;
    this.slimClient = slimClient;
    this.testSystemListener = new CompositeTestSystemListener();
  }

  public SlimTestContext getTestContext() {
    return testContext;
  }

  @Override
  public String getName() {
    return testSystemName;
  }

  @Override
  public boolean isSuccessfullyStarted() {
    return true;
  }

  @Override
  public void start() throws IOException {
    try {
      slimClient.start();
      testSystemListener.testSystemStarted(this);
    } catch (SlimError e) {
      exceptionOccurred(e);
    }
  }

  @Override
  public void kill() throws IOException {
    // No need to send events here, since killing the process is typically done asynchronously.
    slimClient.kill();
  }

  @Override
  public void bye() throws IOException {
    try {
      slimClient.bye();
      testSystemStopped(null);
    } catch (IOException e) {
      exceptionOccurred(e);
      throw e;
    } catch (Exception e) {
      exceptionOccurred(e);
    }
  }

  @Override
  public void runTests(TestPage pageToTest) throws IOException {
    initializeTest();

    testStarted(pageToTest);
    processAllTablesOnPage(pageToTest);
    testComplete(pageToTest, testContext.getTestSummary());
  }

  public void addTestSystemListener(TestSystemListener listener) {
    testSystemListener.addTestSystemListener(listener);
  }

  private void initializeTest() {
    testContext = new SlimTestContextImpl();
    stopTestCalled = false;
  }

  protected abstract void processAllTablesOnPage(TestPage testPage) throws IOException;

  protected void processTable(SlimTable table) throws IOException, SyntaxError {
    List<SlimAssertion> assertions = createAssertions(table);
    Map<String, Object> instructionResults;
    if (!stopTestCalled) {
      // Okay, if this crashes, the test system is killed.
      // We're not gonna continue here, but instead declare our test system done.
      try {
        instructionResults = slimClient.invokeAndGetResponse(SlimAssertion.getInstructions(assertions));
      } catch (IOException e) {
        exceptionOccurred(e);
        throw e;
      }
    } else {
      instructionResults = Collections.emptyMap();
    }

    evaluateTables(assertions, instructionResults);
  }

  private List<SlimAssertion> createAssertions(SlimTable table) throws SyntaxError {
    List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
    assertions.addAll(table.getAssertions());
    return assertions;
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

  protected void evaluateTables(List<SlimAssertion> assertions, Map<String, Object> instructionResults) {
    for (SlimAssertion a : assertions) {
      try {
        final String key = a.getInstruction().getId();
        final Object returnValue = instructionResults.get(key);
        if (returnValue != null && returnValue instanceof String && ((String)returnValue).startsWith(EXCEPTION_TAG)) {
          SlimExceptionResult exceptionResult = makeExceptionResult(key, (String) returnValue);
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

  private SlimExceptionResult makeExceptionResult(String resultKey, String resultString) {
    SlimExceptionResult exceptionResult = new SlimExceptionResult(resultKey, resultString);
    return exceptionResult;
  }

  protected void testOutputChunk(String output) throws IOException {
    testSystemListener.testOutputChunk(output);
  }

  protected void testStarted(TestPage testPage) throws IOException {
    testSystemListener.testStarted(testPage);
  }

  protected void testComplete(TestPage testPage, TestSummary testSummary) throws IOException {
    testSystemListener.testComplete(testPage, testSummary);
  }

  protected void exceptionOccurred(Throwable e) {
    try {
      slimClient.kill();
    } catch (IOException e1) {
      LOG.log(Level.WARNING, "Failed to kill SLiM client", e);
    }
    testSystemStopped(e);
  }

  protected void testAssertionVerified(Assertion assertion, TestResult testResult) {
    testSystemListener.testAssertionVerified(assertion, testResult);
  }

  protected void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    testSystemListener.testExceptionOccurred(assertion, exceptionResult);
  }

  // Ensure testSystemStopped is called only once per test system. First call counts.
  protected void testSystemStopped(Throwable e) {
    if (testSystemIsStopped) return;

    testSystemIsStopped = true;
    ExecutionLog log = slimClient.getExecutionLog();
    if (e != null) {
      log.addException(e);
    }
    testSystemListener.testSystemStopped(this, log, e);
  }
}
