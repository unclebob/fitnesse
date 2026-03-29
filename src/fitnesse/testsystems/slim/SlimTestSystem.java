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
import fitnesse.wiki.PageData;

import static fitnesse.slim.SlimServer.*;

public abstract class SlimTestSystem implements TestSystem {
  // BULK   - Process a full table by the Slim Client in one go
  // SINGLE - Process a single instruction and give control back to the test system
  // Default Mode is BULK
  public static final String SLIM_MODE = "slim.mode";
  // This will force the mode SINGLE to allow a more interactive user experience
  public static final String SLIM_SHOW = "slim.show";
  private final SlimClient slimClient;
  private final CompositeTestSystemListener testSystemListener;
  private final String testSystemName;

  private SlimTestContextImpl testContext;
  private boolean stopTestCalled;
  private boolean ignoreAllTestsCalled;
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

  private boolean isModeSingle(){
    String sbys = getTestContext().getPageToTest().getVariable(SLIM_MODE);
    if (sbys != null){
      if ("SINGLE".equals(sbys)) return true;
    }
    sbys = getTestContext().getPageToTest().getVariable(SLIM_SHOW);
    if (sbys != null){
       return true;
    }
    return false;
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
    } catch (Exception e) {
      stopTestSystem(e);
      throw e;
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

  protected void initializeTest(TestPage testPage) {
    testContext = createTestContext(testPage);
    stopTestCalled = false;
    ignoreAllTestsCalled = false;
  }

  protected SlimTestContextImpl createTestContext(TestPage testPage) {
    return new SlimTestContextImpl(testPage);
  }

  protected abstract void processAllTablesOnPage(TestPage testPage) throws TestExecutionException;

  protected void processTable(SlimTable table, boolean isSuiteTearDownPage) throws TestExecutionException {
    List<SlimAssertion> assertions = table.getAssertions();
     Map<String, Object> instructionResults;
    if (stopTestCalled && !table.isTearDown()) {
      instructionResults = Collections.emptyMap();
      evaluateTables(assertions, instructionResults);
    } else if (ignoreAllTestsCalled && !table.isTearDown()){
      instructionResults = Collections.emptyMap();
      evaluateTables(assertions, instructionResults);
    } else {
      boolean tearDownOfAlreadyStartedTest = stopTestCalled && table.isTearDown();
      if(ignoreAllTestsCalled && table.isTearDown()){
        ignoreAllTestsCalled = false;
      }
      if (stopSuiteCalled && !isSuiteTearDownPage && !tearDownOfAlreadyStartedTest) {
        instructionResults = Collections.emptyMap();
        evaluateTables(assertions, instructionResults);
      } else {
        if (isModeSingle()){
          boolean IgnoreTestTable = false;
          for (SlimAssertion assertion : assertions) {
            List<SlimAssertion> oneAssertion = new ArrayList<>();
            oneAssertion.add(assertion);
            testAssertionWillBeExecuted(assertion);
            instructionResults = slimClient.invokeAndGetResponse(SlimAssertion.getInstructions(oneAssertion));
            final String instructionId = assertion.getInstruction().getId();
            Object instructionResult = instructionResults.get(instructionId);
            IgnoreTestTable = evaluateAssertion(instructionResult,IgnoreTestTable, assertion, instructionId);
          }
        } else {
            instructionResults = slimClient.invokeAndGetResponse(SlimAssertion.getInstructions(assertions));
            evaluateTables(assertions, instructionResults);
        }
      }
    }

    
  }

  protected void evaluateTables(List<SlimAssertion> assertions, Map<String, Object> instructionResults) throws SlimCommunicationException {
    boolean IgnoreTestTable = false;
    for (SlimAssertion a : assertions) {
      final String key = a.getInstruction().getId();
      Object instructionResult = instructionResults.get(key);
      IgnoreTestTable = evaluateAssertion(instructionResult, IgnoreTestTable, a, key);
    }
  }

  private boolean evaluateAssertion(Object InstructionResult, boolean IgnoreTestTable, SlimAssertion a, String key)
     throws SlimCommunicationException {
    

    //Ignore management
    if(!ignoreAllTestsCalled) {
      if (InstructionResult != null && InstructionResult.toString().contains(EXCEPTION_IGNORE_ALL_TESTS_TAG)) {
        ignoreAllTestsCalled = IgnoreTestTable = true;
      } else if (InstructionResult != null && InstructionResult.toString().contains(EXCEPTION_IGNORE_SCRIPT_TEST_TAG)) {
        IgnoreTestTable = true;
      } else if (IgnoreTestTable) {
        InstructionResult = "IGNORE_SCRIPT_TEST";
      }
    } else {
      InstructionResult = "IGNORE_SCRIPT_TEST";
    }
    //Exception management
    if (InstructionResult != null && InstructionResult instanceof String && ((String) InstructionResult).startsWith(EXCEPTION_TAG)) {
      SlimExceptionResult exceptionResult = new SlimExceptionResult(key, (String) InstructionResult);
      if (exceptionResult.isStopTestException()) {
        //IgnoreTestTable = stopTestCalled = true;
        stopTestCalled = true;
        stopSuiteCalled = PageData.SUITE_SETUP_NAME.equals(testContext.getPageToTest().getName());
      }
      if (exceptionResult.isStopSuiteException()) {
        //IgnoreTestTable = stopTestCalled = stopSuiteCalled = true;
        stopTestCalled = stopSuiteCalled = true;
      }
      exceptionResult = a.getExpectation().evaluateException(exceptionResult);
      if (exceptionResult != null) {
        if (!exceptionResult.isCatchException())
          testExceptionOccurred(a, exceptionResult);
        else
          testAssertionVerified(a, exceptionResult.catchTestResult());
      } else {
        // Silently ignored exception for optional decision table functions
        // see class SilentReturnExpectation
        testAssertionVerified(a, null);
      }

    } else {
      //Normal results
      TestResult testResult = a.getExpectation().evaluateExpectation(InstructionResult);
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
    return IgnoreTestTable;
  }

  protected void testOutputChunk(TestPage testPage, String output) {
    testSystemListener.testOutputChunk(testPage, output);
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

  protected void testAssertionWillBeExecuted(Assertion assertion) {
    testSystemListener.testAssertionWillBeExecuted(assertion);
  }
  // Ensure testSystemStopped is called only once per test system. First call counts.
  protected void testSystemStopped(Throwable e) {
    if (testSystemIsStopped) return;
    testSystemIsStopped = true;
    testSystemListener.testSystemStopped(this, e);
  }
}
