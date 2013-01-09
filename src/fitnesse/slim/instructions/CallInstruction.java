package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;

import static util.ListUtility.list;

public class CallInstruction implements Instruction<CallInstruction.CallExecutor> {
  public static final String INSTRUCTION = "call";
  private String id;
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallInstruction(String id, String instanceName, String methodName, Object[] args,
                         NameTranslator methodNameTranslator) {
    this.id = id;
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  public Object execute(CallExecutor executor) {
    Object result = executor.call(this.instanceName, this.methodName, this.args);
    return list(id, result);
  }

  public static interface CallExecutor extends InstructionExecutor {
    Object call(String instanceName, String methodName, Object... arguments);
  }
}
