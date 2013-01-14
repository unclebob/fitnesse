package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

import static java.lang.String.format;

public class InvalidInstruction extends Instruction<InstructionExecutor> {
  private final String operation;

  public InvalidInstruction(String id, String operation) {
    super(id);
    this.operation = operation;
  }

  @Override
  protected Object executeInternal(InstructionExecutor executor) throws SlimException {
    throw new SlimException(format("message:<<INVALID_STATEMENT: %s>>", operation));
  }
}
