package fitnesse.testsystems.slim;

import fitnesse.slim.SlimException;
import fitnesse.slim.SlimServer;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.InstructionExecutor;
import fitnesse.slim.instructions.InstructionResult;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimExpectation;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static fitnesse.slim.SlimServer.EXCEPTION_TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * See also FitNesseRoot/FitNesse/SuiteAcceptanceTests/SuiteSlimTests, StopSuite, StopTestInSuiteSetUp and StopTest in particular
 */
public class SlimTestSystemTableProcessingTest {

  private final RecordingTestSystemListener listener = new RecordingTestSystemListener();
  private final DummySlimTestSystem slimTestSystem = new DummySlimTestSystem();

  @Before
  public void setup() {
    slimTestSystem.addTestSystemListener(listener);
    slimTestSystem.newTestPage();
  }

  @Test
  public void normalFlowTwoTablesSamePage() throws TestExecutionException {
    slimTestSystem.processTable(table("Table1"), false);
    slimTestSystem.processTable(table("Table2"), false);

    assertTestRecords(pass("Table1"), pass("Table2"));
  }

  @Test
  public void tableFollowingRandomExceptionStillExecuted() throws TestExecutionException {
    String exceptionId = EXCEPTION_TAG + "table1 with random exception";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.processTable(table("Table2"), false);

    assertTestRecords(error(exceptionId), pass("Table2"));
  }

  @Test
  public void tableFollowingIgnoreScriptTestExceptionExecuted() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_IGNORE_SCRIPT_TEST_TAG + "table1 with random ignore exception";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.processTable(table("Table2"), false);
    slimTestSystem.processTable(table("Table3"), false);

    assertTestRecords(ignore(exceptionId), pass("Table2"), pass("Table3"));
  }

  @Test
  public void tableFollowingIgnoreAllTestsExceptionIgnored() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_IGNORE_ALL_TESTS_TAG + "table1 with random ignore exception";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.processTable(table("Table2"), false);
    slimTestSystem.processTable(table("Table3"), false);

    assertTestRecords(ignore(exceptionId), ignore("Table2"), ignore("Table3"));
  }

  @Test
  public void tableFollowingStopTestExceptionSkipped() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_TEST_TAG + "StopTestException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.processTable(table("Table2"), false);

    assertTestRecords(fail(exceptionId), ignore("Table2"));
  }

  @Test
  public void nextTestFollowingStopTestExceptionExecuted() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_TEST_TAG + "StopTestException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.newTestPage();
    slimTestSystem.processTable(table("NextTest"), false);

    assertTestRecords(fail(exceptionId), pass("NextTest"));
  }

  @Test
  public void tearDownFollowingStopTestExceptionStillExecuted() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_TEST_TAG + "StopTestException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.processTable(tearDownTable("TearDown"), false);

    assertTestRecords(fail(exceptionId), pass("TearDown"));
  }

  @Test
  public void suiteTearDownFollowingStopTestExceptionStillExecuted() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_TEST_TAG + "StopTestException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.newTestPage();
    slimTestSystem.processTable(table("SuiteTearDown"), true);
    assertTestRecords(fail(exceptionId), pass("SuiteTearDown"));
  }

  @Test
  public void nextPageAndItsTeardownShouldBeSkipped() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_SUITE_TAG + "StopSuiteException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.newTestPage();
    slimTestSystem.processTable(table("NextPage"), false);
    slimTestSystem.processTable(tearDownTable("NextPageTearDown"), false);

    assertTestRecords(error(exceptionId), ignore("NextPage"), ignore("NextPageTearDown"));
  }

  @Test
  public void nextPageAndItsTeardownShouldBeSkippedOnStopTestInSuiteSetUp() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_TEST_TAG + "StopTestException";
    slimTestSystem.newTestPage("SuiteSetUp");
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.newTestPage();
    slimTestSystem.processTable(table("NextPage"), false);
    slimTestSystem.processTable(tearDownTable("NextPageTearDown"), false);

    assertTestRecords(fail(exceptionId), ignore("NextPage"), ignore("NextPageTearDown"));
  }

  @Test
  public void suiteTearDownAlsoOnSuiteStopExceptionExecuted() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_SUITE_TAG + "StopSuiteException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.newTestPage();
    slimTestSystem.processTable(table("SuiteTearDown"), true);

    assertTestRecords(error(exceptionId), pass("SuiteTearDown"));
  }

  @Test
  public void tearDownThisPageStopSuiteExceptionShouldStillBeExecuted() throws TestExecutionException {
    String exceptionId = SlimServer.EXCEPTION_STOP_SUITE_TAG + "StopSuiteException";
    slimTestSystem.processTable(table(exceptionId), false);
    slimTestSystem.processTable(tearDownTable("ThisPageTeardown"), false);

    assertTestRecords(error(exceptionId), pass("ThisPageTeardown"));
  }

  private static DummySlimTable table(String exceptionId) {
    return new DummySlimTable(exceptionId);
  }

  private static SlimTable tearDownTable(String key) {
    DummySlimTable result = table(key);
    result.setTearDown(true);
    return result;
  }

  public void assertTestRecords(String... testRecords) {
    assertEquals(Arrays.asList(testRecords), listener.getRecordedHistory());
  }

  public static String pass(String key) {
    return formatTestRecord(ExecutionResult.PASS, key);
  }

  public static String ignore(String key) {
    return formatTestRecord(ExecutionResult.IGNORE, key);
  }

  public static String error(String key) {
    return formatTestRecord(ExecutionResult.ERROR, key);
  }

  public static String fail(String key) {
    return formatTestRecord(ExecutionResult.FAIL, key);
  }

  private static String formatTestRecord(ExecutionResult executionResult, String key) {
    return String.format("%s(\"%s\")", executionResult, key);
  }

  private static class RecordingTestSystemListener implements TestSystemListener {

    private final List<String> recordedHistory = new ArrayList<>();

    @Override
    public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
      assertNotEquals("Exceptions are not expected to indicate success", exceptionResult.getExecutionResult(), ExecutionResult.PASS);
      record(assertion, exceptionResult.getExecutionResult());
    }

    private void record(Assertion assertion, ExecutionResult executionResult) {
      recordedHistory.add(formatTestRecord(executionResult, assertion.getInstruction().getId()));
    }

    public List<String> getRecordedHistory() {
      return recordedHistory;
    }

    @Override
    public void testSystemStarted(TestSystem testSystem) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void testOutputChunk(TestPage testPage, String output) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void testStarted(TestPage testPage) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void testComplete(TestPage testPage, TestSummary testSummary) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void testSystemStopped(TestSystem testSystem, Throwable cause) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void testAssertionVerified(Assertion assertion, TestResult testResult) {
      ExecutionResult executionResult = testResult.getExecutionResult();
      record(assertion, executionResult);
    }
  }

  private static class DummySlimTable extends SlimTable {

    private final List<SlimAssertion> assertions;
    private boolean tearDown;

    public DummySlimTable(String assertionId) {
      super(null, null, null);
      this.assertions = Collections.singletonList(
          new SlimAssertion(new DummyInstruction(assertionId),
              new IgnoreOnNullPassOtherwiseSlimExpectation())
      );
    }

    public void setTearDown(boolean tearDown) {
      this.tearDown = tearDown;
    }

    @Override
    public boolean isTearDown() {
      return tearDown;
    }

    @Override
    protected String getTableType() {
      return "test";
    }

    @Override
    public List<SlimAssertion> getAssertions() throws TestExecutionException {
      return assertions;
    }
  }

  private static class DummyInstruction extends Instruction {
    public DummyInstruction(String assertion) {
      super(assertion);
    }

    @Override
    protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
      throw new UnsupportedOperationException();
    }
  }

  private static class DummySlimTestSystem extends SlimTestSystem {

    public final InstructionIdMirroringSlimClient slimClientMock;

    public DummySlimTestSystem() {
      this(new InstructionIdMirroringSlimClient());
    }

    public DummySlimTestSystem(InstructionIdMirroringSlimClient slimClientMock) {
      super(null, slimClientMock);
      this.slimClientMock = slimClientMock;
    }

    @Override
    protected void processAllTablesOnPage(TestPage testPage) throws TestExecutionException {
      throw new UnsupportedOperationException();
    }

    public void newTestPage() {
      initializeTest(new WikiTestPage(new WikiPageDummy()));
    }

    public void newTestPage(String pageName) {
      initializeTest(new WikiTestPage(new WikiPageDummy(pageName, "", null)));
    }
  }

  private static class InstructionIdMirroringSlimClient implements SlimClient {
    @Override
    public void start() throws IOException, SlimVersionMismatch {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws SlimCommunicationException {
      Map<String, Object> response = new HashMap<>();
      for (Instruction statement : statements) {
        response.put(statement.getId(), statement.getId());
      }
      return response;
    }

    @Override
    public void connect() throws IOException, SlimVersionMismatch {
      throw new UnsupportedOperationException();
    }

    @Override
    public void bye() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void kill() {
      throw new UnsupportedOperationException();
    }
  }

  private static class IgnoreOnNullPassOtherwiseSlimExpectation implements SlimExpectation {
    @Override
    public TestResult evaluateExpectation(Object returnValues) {
      return new SlimTestResult((null == returnValues) || (returnValues.toString().contains("IGNORE_SCRIPT_TEST")) ? ExecutionResult.IGNORE : ExecutionResult.PASS);
    }

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
      return exceptionResult;
    }
  }
}
