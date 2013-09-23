package fitnesse.testsystems;

public interface TestResult {
  boolean doesCount();

  boolean hasActual();

  String getActual();

  boolean hasExpected();

  String getExpected();

  boolean hasMessage();

  String getMessage();

  ExecutionResult getExecutionResult();
}
