package fitnesse.testsystems;

import fitnesse.slim.instructions.Instruction;

public interface Assertion {
  Instruction getInstruction();

  Expectation getExpectation();
}
