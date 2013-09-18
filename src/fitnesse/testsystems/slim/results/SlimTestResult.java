package fitnesse.testsystems.slim.results;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;

public class SlimTestResult implements TestResult {

  private final String actual;
  private final String expected;
  private final ExecutionResult executionResult;
  private final String message;
  private final boolean counts;

  public SlimTestResult(ExecutionResult executionResult) {
    this(null, null, null, executionResult, true);
  }

  protected SlimTestResult(String actual, String expected, String message, ExecutionResult executionResult) {
    this(actual, expected, message, executionResult, true);
  }

  protected SlimTestResult(String actual, String expected, String message, ExecutionResult executionResult, boolean counts) {
    this.actual = actual;
    this.expected = expected;
    this.message = message;
    this.executionResult = executionResult;
    this.counts = counts;
  }

  @Override
  public boolean doesCount() {
    return counts;
  }
  @Override
  public boolean hasActual() {
    return actual != null;
  }

  @Override
  public String getActual() {
    return actual;
  }

  @Override
  public boolean hasExpected() {
    return expected != null;
  }

  @Override
  public String getExpected() {
    return expected;
  }

  @Override
  public boolean hasMessage() {
    return message != null;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public ExecutionResult getExecutionResult() {
    return executionResult;
  }

  public SlimTestResult negateTestResult() {
    ExecutionResult newExecutionResult;
    if (executionResult == ExecutionResult.PASS) {
      newExecutionResult = ExecutionResult.FAIL;
    } else if (executionResult == ExecutionResult.FAIL) {
      newExecutionResult = ExecutionResult.PASS;
    } else  {
      newExecutionResult = executionResult;
    }
    return new SlimTestResult(actual, expected, message, newExecutionResult);
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

  public static SlimTestResult pass() {
    return new SlimTestResult(ExecutionResult.PASS);
  }

  public static SlimTestResult pass(String message) {
    return new SlimTestResult(null, null, message, ExecutionResult.PASS);
  }

  // For negating checks:
  public static TestResult pass(String actual, String expected) {
    return new SlimTestResult(actual, expected, null, ExecutionResult.PASS);
  }

  public static SlimTestResult fail() {
    return new SlimTestResult(null, null, null, ExecutionResult.FAIL);
  }

  public static SlimTestResult fail(String message) {
    return new SlimTestResult(null, null, message, ExecutionResult.FAIL);
  }

  public static SlimTestResult fail(String actual, String expected) {
    return new SlimTestResult(actual, expected, null, ExecutionResult.FAIL);
  }

  public static SlimTestResult fail(String actual, String expected, String message) {
    return new SlimTestResult(actual, expected, message, ExecutionResult.FAIL);
  }

  public static SlimTestResult ignore() {
    return new SlimTestResult(ExecutionResult.IGNORE);
  }

  public static SlimTestResult ignore(String message) {
    return new SlimTestResult(null, null, message, ExecutionResult.IGNORE);
  }

  public static SlimTestResult error(String message) {
    return new SlimTestResult(null, null, message, ExecutionResult.ERROR);
  }

  public static SlimTestResult error(String message, String actual) {
    return new SlimTestResult(actual, null, message, ExecutionResult.ERROR);
  }

  public static SlimTestResult plain() {
    return new SlimTestResult(null, null, null, null);
  }

  public static SlimTestResult plain(String message) {
   return new SlimTestResult(null, null, message, null);
  }

  // pass without counting
  public static SlimTestResult ok(String message) {
    return new SlimTestResult(null, null, message, ExecutionResult.PASS, false);
  }
}
