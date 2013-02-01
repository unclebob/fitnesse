package fitnesse.testsystems.slim.tables;


import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;

public interface Expectation {

  Expectation NOOP_EXPECTATION = new Expectation() {
    @Override public TestResult evaluateExpectation(Object returnValues) { return null; }
    @Override public ExceptionResult evaluateException(ExceptionResult exceptionResult) { return null; }
  };

  TestResult evaluateExpectation(Object returnValues);

  ExceptionResult evaluateException(ExceptionResult exceptionResult);
}