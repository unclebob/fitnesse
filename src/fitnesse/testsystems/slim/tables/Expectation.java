package fitnesse.testsystems.slim.tables;

import java.util.Map;


public interface Expectation {

  // TODO: put an InstructionResult here or something like that.
  void evaluateExpectation(Map<String, Object> returnValues);

  String getInstructionTag();
}