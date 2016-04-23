package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.Assertion;

public class SlimAssertion implements Assertion {
  private final Instruction instruction;
  private final SlimExpectation expectation;

  public SlimAssertion(Instruction instruction, SlimExpectation expectation) {
    this.instruction = instruction;
    this.expectation = expectation;
  }

  @Override
  public fitnesse.testsystems.Instruction getInstruction() {
    return new fitnesse.testsystems.Instruction() {
      @Override
      public String getId() {
        return instruction.getId();
      }

      @Override
      public String toString() {
        return instruction.toString();
      }
    };
  }

  @Override
  public SlimExpectation getExpectation() {
    return expectation;
  }

  @Override
  public String toString() {
    return String.format("instruction: %s%nassertion: %s%n", instruction.toString(), expectation.toString());
  }

  /**
   * Get Instructions from the assertions; NOOP's are filtered out.
   *
   * @param assertions List of SlimAssertions to filter
   * @return all instructions from the input without NOOP's
   */
  public static List<Instruction> getInstructions(List<SlimAssertion> assertions) {
    List<Instruction> instructions = new ArrayList<>(assertions.size());
    for (SlimAssertion a : assertions) {
      if (a.instruction != Instruction.NOOP_INSTRUCTION) {
        instructions.add(a.instruction);
      }
    }
    return instructions;
  }

  public static void evaluateExpectations(List<SlimAssertion> assertions, Map<String, Object> results) {
    for (SlimAssertion a : assertions) {
      Object returnValue = results.get(a.getInstruction().getId());
      a.getExpectation().evaluateExpectation(returnValue);
    }
  }


}
