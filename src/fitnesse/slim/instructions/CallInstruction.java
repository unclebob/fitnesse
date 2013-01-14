package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimException;

public class CallInstruction extends Instruction<CallInstruction.CallExecutor> {
  public static final String INSTRUCTION = "call";
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallInstruction(String id, String instanceName, String methodName, Object[] args,
      NameTranslator methodNameTranslator) {
    super(id);
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  protected Object executeInternal(CallExecutor executor) throws SlimException {
    return executor.call(this.instanceName, this.methodName, this.args);
  }

  public static interface CallExecutor extends InstructionExecutor {
    Object call(String instanceName, String methodName, Object... arguments) throws SlimException;
  }
}
