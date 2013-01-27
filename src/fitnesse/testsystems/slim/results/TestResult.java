package fitnesse.testsystems.slim.results;

import fitnesse.testsystems.ExecutionResult;

/**
 * Created with IntelliJ IDEA.
 * User: arjan
 * Date: 1/27/13
 * Time: 10:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestResult {

  private final String actual;
  private final String expected;
  private final ExecutionResult executionResult;

  public TestResult(ExecutionResult executionResult) {
    this(null, null, executionResult);
  }

  public TestResult(String actual, String expected, ExecutionResult executionResult) {
    this.actual = actual;
    this.expected = expected;
    this.executionResult = executionResult;
  }

  public String getActual() {
    return actual;
  }

  public String getExpected() {
    return expected;
  }

  public ExecutionResult getExecutionResult() {
    return executionResult;
  }
}
