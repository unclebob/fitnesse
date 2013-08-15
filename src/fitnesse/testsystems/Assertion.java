package fitnesse.testsystems;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.tables.Expectation;

public interface Assertion {
  Instruction getInstruction();

  Expectation getExpectation();
}
