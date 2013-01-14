package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;
import fitnesse.slim.SlimServer;

import static java.lang.String.format;
import static util.ListUtility.list;

public abstract class Instruction<T extends InstructionExecutor> {
  private String id;

  public Instruction(String id) {
    this.id = id;
  }

  public Object execute(T executor) {
    Object result;
    try {
      result = executeInternal(executor);
    } catch (SlimException e) {
      result = format("%s%s", SlimServer.EXCEPTION_TAG, e.getMessage());
    }
    return list(id, result);
  }

  protected abstract Object executeInternal(T executor) throws SlimException;
}
