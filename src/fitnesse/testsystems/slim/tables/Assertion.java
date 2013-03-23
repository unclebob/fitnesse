package fitnesse.testsystems.slim.tables;

import fitnesse.slim.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Assertion {
  private final Instruction instruction;
  private final Expectation expectation;

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

  public static void evaluateExpectations(List<Assertion> assertions, Map<String, Object> results) {
    for (Assertion a : assertions) {
      Object returnValue = results.get(a.getInstruction().getId());
      a.getExpectation().evaluateExpectation(returnValue);
    }
  }


}