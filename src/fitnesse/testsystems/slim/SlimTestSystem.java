// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fitnesse.slim.instructions.AssignInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimTable;

import static fitnesse.slim.SlimServer.*;

public abstract class SlimTestSystem implements TestSystem {
  private final SlimClient slimClient;
  private final CompositeTestSystemListener testSystemListener;
  private final String testSystemName;

  private SlimTestContextImpl testContext;
  boolean stopTestCalled;
  private boolean stopSuiteCalled;
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
    return !testSystemIsStopped;
  }

  @Override
  public void start() throws UnableToStartException {
    try {
      slimClient.start();
    } catch (SlimVersionMismatch slimVersionMismatch) {
      stopTestSystem(slimVersionMismatch);
      return;
    } catch (IOException e) {
      stopTestSystem(e);
      throw new UnableToStartException("Could not start test system", e);
    }
    testSystemListener.testSystemStarted(this);
  }

  @Override
  public void kill() {
    slimClient.kill();
  }

  @Override
  public void bye() throws UnableToStopException {
    if (testSystemIsStopped) return;
    try {
      slimClient.bye();
      testSystemStopped(null);
    } catch (IOException e) {
      stopTestSystem(e);
      throw new UnableToStopException("Could not stop test system", e);
    }
  }

  @Override
  public void runTests(TestPage pageToTest) throws TestExecutionException {
    initializeTest(pageToTest);

    testStarted(pageToTest);
    try {
      processAllTablesOnPage(pageToTest);
      testComplete(pageToTest, testContext.getTestSummary());
    } catch (Exception e) {
      stopTestSystem(e);
      throw new TestExecutionException(e);
    }
  }

  @Override
  public void addTestSystemListener(TestSystemListener listener) {
    testSystemListener.addTestSystemListener(listener);
  }

  private void initializeTest(TestPage testPage) {
    testContext = createTestContext(testPage);
    stopTestCalled = false;
  }

  protected SlimTestContextImpl createTestContext(TestPage testPage) {
    return new SlimTestContextImpl(testPage);
  }

  protected abstract void processAllTablesOnPage(TestPage testPage) throws TestExecutionException;

  protected void processTable(SlimTable table, boolean isSuiteTearDownPage) throws TestExecutionException {
    List<SlimAssertion> assertions = table.getAssertions();
    final Map<String, Object> instructionResults;
    if (stopTestCalled && !table.isTearDown()) {
      instructionResults = Collections.emptyMap();
    } else {
      boolean tearDownOfAlreadyStartedTest = stopTestCalled && table.isTearDown();
      if (stopSuiteCalled && !isSuiteTearDownPage && !tearDownOfAlreadyStartedTest) {
        instructionResults = Collections.emptyMap();
      } else {
        instructionResults = slimClient.invokeAndGetResponse(SlimAssertion.getInstructions(assertions));
      }
    }

    evaluateTables(assertions, instructionResults);
  }

  protected void evaluateTables(List<SlimAssertion> assertions, Map<String, Object> instructionResults) throws SlimCommunicationException {
    for (SlimAssertion a : assertions) {
      final String key = a.getInstruction().getId();
      final Object returnValue = instructionResults.get(key);
      //Exception management
      if (returnValue != null && returnValue instanceof String && ((String) returnValue).startsWith(EXCEPTION_TAG)) {
        SlimExceptionResult exceptionResult = new SlimExceptionResult(key, (String) returnValue);
        if (exceptionResult.isStopTestException()) {
          stopTestCalled = true;
        }
        if (exceptionResult.isStopSuiteException()) {
          stopTestCalled = stopSuiteCalled = true;
        }
        exceptionResult = a.getExpectation().evaluateException(exceptionResult);
        if (exceptionResult != null) {
          testExceptionOccurred(a, exceptionResult);
        }
      } else {
        //Normal results
        TestResult testResult = a.getExpectation().evaluateExpectation(returnValue);
        testAssertionVerified(a, testResult);

        //Retrieve variables set during expectation step
        if (testResult != null) {
          Map<String, ?> variables = testResult.getVariablesToStore();
          if (variables != null) {
            List<Instruction> instructions = new ArrayList<>(variables.size());
            int i = 0;
            for (Entry<String, ?> variable : variables.entrySet()) {
              instructions.add(new AssignInstruction("assign_" + i++, variable.getKey(), variable.getValue()));
            }
            //Store variables in context
            if (i > 0) {
              slimClient.invokeAndGetResponse(instructions);
            }
          }
        }
      }
    }
  }

  protected void testOutputChunk(String output) {
    testSystemListener.testOutputChunk(output);
  }

  protected void testStarted(TestPage testPage) {
    testSystemListener.testStarted(testPage);
  }

  protected void testComplete(TestPage testPage, TestSummary testSummary) {
    testSystemListener.testComplete(testPage, testSummary);
  }

  protected void stopTestSystem(Throwable e) {
    slimClient.kill();
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
    testSystemListener.testSystemStopped(this, e);
  }
}
