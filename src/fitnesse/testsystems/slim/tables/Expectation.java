package fitnesse.testsystems.slim.tables;


import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.results.ExceptionResult;

public interface Expectation {

  Expectation NOOP_EXPECTATION = new Expectation() {
    @Override public TestResult evaluateExpectation(Object returnValues) { return null; }
    @Override public ExceptionResult evaluateException(ExceptionResult exceptionResult) { return null; }
  };

  TestResult evaluateExpectation(Object returnValues);

  ExceptionResult evaluateException(ExceptionResult exceptionResult);
}