package fitnesse.testsystems.slim.tables;


public interface Expectation {

  // TODO: put an InstructionResult here or something like that.
  void evaluateExpectation(Object returnValues);

  String getInstructionTag();
}