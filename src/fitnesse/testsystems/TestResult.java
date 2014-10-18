package fitnesse.testsystems;

import java.util.Map;

public interface TestResult {
  boolean doesCount();

  boolean hasActual();

  String getActual();

  boolean hasExpected();

  String getExpected();

  boolean hasMessage();

  String getMessage();

  ExecutionResult getExecutionResult();

  Map<String, ?> getVariablesToStore();
}
