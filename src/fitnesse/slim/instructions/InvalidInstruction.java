package fitnesse.slim.instructions;

import fitnesse.slim.SlimServer;

import static java.lang.String.format;
import static util.ListUtility.list;

public class InvalidInstruction implements Instruction<InstructionExecutor> {
  private final String id;
  private final String operation;

  public InvalidInstruction(String id, String operation) {
    this.id = id;
    this.operation = operation;
  }

  @Override
  public Object execute(InstructionExecutor executor) {
    return list(id, format("%smessage:<<INVALID_STATEMENT: %s.>>", SlimServer.EXCEPTION_TAG, operation));
  }
}
