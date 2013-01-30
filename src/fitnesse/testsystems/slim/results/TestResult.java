package fitnesse.testsystems.slim.results;

import fitnesse.testsystems.ExecutionResult;

public class TestResult {

  private final String actual;
  private final String expected;
  private final ExecutionResult executionResult;
  private final String message;
  private final boolean counts;

  public TestResult(ExecutionResult executionResult) {
    this(null, null, null, executionResult, true);
  }

  protected TestResult(String actual, String expected, String message, ExecutionResult executionResult) {
    this(actual, expected, message, executionResult, true);
  }

  protected TestResult(String actual, String expected, String message, ExecutionResult executionResult, boolean counts) {
    this.actual = actual;
    this.expected = expected;
    this.message = message;
    this.executionResult = executionResult;
    this.counts = counts;
  }

  public boolean doesCount() {
    return counts;
  }
  public boolean hasActual() {
    return actual != null;
  }

  public String getActual() {
    return actual;
  }

  public boolean hasExpected() {
    return expected != null;
  }

  public String getExpected() {
    return expected;
  }

  public boolean hasMessage() {
    return message != null;
  }

  public String getMessage() {
    return message;
  }

  public ExecutionResult getExecutionResult() {
    return executionResult;
  }

  public TestResult negateTestResult() {
    ExecutionResult newExecutionResult;
    if (executionResult == ExecutionResult.PASS) {
      newExecutionResult = ExecutionResult.FAIL;
    } else if (executionResult == ExecutionResult.FAIL) {
      newExecutionResult = ExecutionResult.PASS;
    } else  {
      newExecutionResult = executionResult;
    }
    return new TestResult(actual, expected, message, newExecutionResult);
  }

  public String toString(String originalContent) {
    StringBuilder builder = new StringBuilder();
    if (executionResult != null) {
      builder.append(executionResult.toString()).append('(');
    }
    if (hasActual()) {
      builder.append("a=").append(getActual());
      if (hasExpected() || hasMessage()) {
        builder.append(";");
      }
    }
    if (hasExpected()) {
      builder.append("e=").append(getExpected());
      if (hasMessage()) {
        builder.append(";");
      }
    }
    if (hasMessage()) {
      builder.append(getMessage());
    } else if (!hasActual() && !hasExpected()) {
      builder.append(originalContent);
    }
    if (executionResult != null) {
      builder.append(')');
    }
    return builder.toString();
  }

  public String toString() {
    return toString("");
  }

  public static TestResult pass() {
    return new TestResult(ExecutionResult.PASS);
  }

  public static TestResult pass(String message) {
    return new TestResult(null, null, message, ExecutionResult.PASS);
  }

  // For negating checks:
  public static TestResult pass(String actual, String expected) {
    return new TestResult(actual, expected, null, ExecutionResult.PASS);
  }

  public static TestResult fail() {
    return new TestResult(null, null, null, ExecutionResult.FAIL);
  }

  public static TestResult fail(String message) {
    return new TestResult(null, null, message, ExecutionResult.FAIL);
  }

  public static TestResult fail(String actual, String expected) {
    return new TestResult(actual, expected, null, ExecutionResult.FAIL);
  }

  public static TestResult fail(String actual, String expected, String message) {
    return new TestResult(actual, expected, message, ExecutionResult.FAIL);
  }

  public static TestResult ignore() {
    return new TestResult(ExecutionResult.IGNORE);
  }

  public static TestResult ignore(String message) {
    return new TestResult(null, null, message, ExecutionResult.IGNORE);
  }

  public static TestResult error(String message) {
    return new TestResult(null, null, message, ExecutionResult.ERROR);
  }

  public static TestResult error(String message, String actual) {
    return new TestResult(actual, null, message, ExecutionResult.ERROR);
  }

  public static TestResult plain() {
    return new TestResult(null, null, null, null);
  }

  public static TestResult plain(String message) {
   return new TestResult(null, null, message, null);
  }

  // pass without counting
  public static TestResult ok(String message) {
    return new TestResult(null, null, message, ExecutionResult.PASS, false);
  }
}
