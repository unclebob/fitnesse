package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.Assertion;

public class SlimAssertion implements Assertion {
  private final Instruction instruction;
  private final Expectation expectation;

  SlimAssertion(Instruction instruction, Expectation expectation) {
    this.instruction = instruction;
    this.expectation = expectation;
  }

  @Override
  public Instruction getInstruction() {
    return instruction;
  }

  @Override
  public Expectation getExpectation() {
    return expectation;
  }

  /**
   * Get Instructions from the assertions; NOOP's are filtered out.
   * @param assertions
   * @return
   */
  public static List<Instruction> getInstructions(List<SlimAssertion> assertions) {
    List<Instruction> instructions = new ArrayList<Instruction>(assertions.size());
    for (SlimAssertion a : assertions) {
      if (a.getInstruction() != Instruction.NOOP_INSTRUCTION) {
        instructions.add(a.getInstruction());
      }
    }
    return instructions;
  }

  public static void evaluateExpectations(List<SlimAssertion> assertions, Map<String, Object> results) {
    for (Assertion a : assertions) {
      Object returnValue = results.get(a.getInstruction().getId());
      a.getExpectation().evaluateExpectation(returnValue);
    }
  }


}