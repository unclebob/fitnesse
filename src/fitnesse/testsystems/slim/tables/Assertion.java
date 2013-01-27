package fitnesse.testsystems.slim.tables;

import fitnesse.slim.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Assertion {
  private final Instruction instruction;
  private final Expectation expectation;
  private String actual;

  Assertion(Instruction instruction, Expectation expectation) {
    this.instruction = instruction;
    this.expectation = expectation;
  }

  public Instruction getInstruction() {
    return instruction;
  }

  public Expectation getExpectation() {
    return expectation;
  }

  public void updateWithActualResult(String actual) {
    this.actual = actual;
  }
  
  /**
   * Get Instructions from the assertions; NOOP's are filtered out.
   * @param assertions
   * @return
   */
  public static List<Instruction> getInstructions(List<Assertion> assertions) {
    List<Instruction> instructions = new ArrayList<Instruction>(assertions.size());
    for (Assertion a : assertions) {
      if (a.getInstruction() != Instruction.NOOP_INSTRUCTION) {
        instructions.add(a.getInstruction());
      }
    }
    return instructions;
  }

  /**
   * Get Expectation's from a list of assertions. NOOP's are filtered out.
   * @param assertions
   * @return
   */
  public static List<Expectation> getExpectations(List<Assertion> assertions) {
    List<Expectation> expectations = new ArrayList<Expectation>(assertions.size());
    for (Assertion a : assertions) {
      if (a.getExpectation() != Expectation.NOOP_EXPECTATION) {
        expectations.add(a.getExpectation());
      }
    }
    return expectations;
  }

  public static void evaluateExpectations(List<Assertion> assertions, Map<String, Object> results) {
    for (Assertion a : assertions) {
      Object returnValue = results.get(a.getInstruction().getId());
      a.getExpectation().evaluateExpectation(returnValue);
    }
  }


}