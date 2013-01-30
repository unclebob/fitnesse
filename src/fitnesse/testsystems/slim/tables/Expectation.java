package fitnesse.testsystems.slim.tables;


import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;

public interface Expectation {

  Expectation NOOP_EXPECTATION = new Expectation() {
    @Override public TestResult evaluateExpectation(Object returnValues) { return null; }
    @Override public void handleException(ExceptionResult exceptionResult) { }
  };

  TestResult evaluateExpectation(Object returnValues);

  void handleException(ExceptionResult exceptionResult);
}