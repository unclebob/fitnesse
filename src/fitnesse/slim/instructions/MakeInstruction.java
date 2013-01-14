package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

public class MakeInstruction extends Instruction<MakeInstruction.MakeExecutor> {
  public static final String INSTRUCTION = "make";
  private final String instanceName;
  private final String className;
  private final Object[] args;

  public MakeInstruction(String id, String instanceName, String className, Object[] args) {
    super(id);
    this.instanceName = instanceName;
    this.className = className;
    this.args = args;
  }

  @Override
  protected Object executeInternal(MakeExecutor executor) throws SlimException {
    return executor.create(instanceName, className, args);
  }

  public static interface MakeExecutor extends InstructionExecutor {
    Object create(String instanceName, String className, Object... constructorArgs) throws SlimException;
  }
}
