package fitnesse.testsystems;

public interface Assertion {
  Instruction getInstruction();

  Expectation getExpectation();
}
