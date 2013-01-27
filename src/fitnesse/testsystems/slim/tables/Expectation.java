package fitnesse.testsystems.slim.tables;


import fitnesse.testsystems.slim.results.ExceptionResult;

public interface Expectation {

  Expectation NOOP_EXPECTATION = new Expectation() {
    @Override public void evaluateExpectation(Object returnValues) { }
    @Override public void handleException(ExceptionResult exceptionResult) { }
  };

  // TODO: put an InstructionResult here or something like that.
  void evaluateExpectation(Object returnValues);

  void handleException(ExceptionResult exceptionResult);
}