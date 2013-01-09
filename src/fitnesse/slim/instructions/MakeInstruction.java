package fitnesse.slim.instructions;

import static util.ListUtility.list;

public class MakeInstruction implements Instruction<MakeInstruction.MakeExecutor> {
  public static final String INSTRUCTION = "make";
  private final String id;
  private final String instanceName;
  private final String className;
  private final Object[] args;

  public MakeInstruction(String id, String instanceName, String className, Object[] args) {
    this.id = id;
    this.instanceName = instanceName;
    this.className = className;
    this.args = args;
  }

  @Override
  public Object execute(MakeExecutor executor) {
    Object instance = executor.create(instanceName, className, args);
    return list(id, instance);
  }

  public static interface MakeExecutor extends InstructionExecutor {
    Object create(String instanceName, String className, Object... constructorArgs);
  }
}
