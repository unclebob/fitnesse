package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;
import fitnesse.slim.SlimServer;

public class InvalidInstruction extends Instruction {
  private final String operation;

  public InvalidInstruction(String id, String operation) {
    super(id);
    this.operation = operation;
  }

  @Override
  protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
    throw new SlimException(operation, SlimServer.MALFORMED_INSTRUCTION, true);
  }
}
