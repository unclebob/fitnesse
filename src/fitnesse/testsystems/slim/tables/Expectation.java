package fitnesse.testsystems.slim.tables;


public interface Expectation {

  Expectation NOOP_EXPECTATION = new Expectation() {
    @Override public void evaluateExpectation(Object returnValues) {
    }
  };

  // TODO: put an InstructionResult here or something like that.
  void evaluateExpectation(Object returnValues);
}