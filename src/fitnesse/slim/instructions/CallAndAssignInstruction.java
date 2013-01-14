package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimException;

public class CallAndAssignInstruction
    extends Instruction<CallAndAssignInstruction.CallAndAssignExecutor> {
  public static final String INSTRUCTION = "callAndAssign";
  private String symbolName;
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName, Object[] args,
      NameTranslator methodNameTranslator) {
    super(id);
    this.symbolName = symbolName;
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  protected Object executeInternal(CallAndAssignExecutor executor) throws SlimException {
    return executor.callAndAssign(symbolName, instanceName, methodName, args);
  }

  public static interface CallAndAssignExecutor extends InstructionExecutor {
    Object callAndAssign(String symbolName, String instanceName, String methodsName, Object... arguments)
        throws SlimException;
  }
}
