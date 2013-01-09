package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;

import static util.ListUtility.list;

public class CallAndAssignInstruction
    implements Instruction<CallAndAssignInstruction.CallAndAssignExecutor> {
  public static final String INSTRUCTION = "callAndAssign";
  private String id;
  private String symbolName;
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName, Object[] args,
                                  NameTranslator methodNameTranslator) {
    this.id = id;
    this.symbolName = symbolName;
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  public Object execute(CallAndAssignExecutor executor) {
    Object result = executor.callAndAssign(symbolName, instanceName, methodName, args);
    return list(id, result);
  }

  public static interface CallAndAssignExecutor extends InstructionExecutor {
    Object callAndAssign(String symbolName, String instanceName, String methodsName, Object... arguments);
  }
}
