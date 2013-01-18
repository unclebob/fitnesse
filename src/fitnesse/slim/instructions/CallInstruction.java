package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimException;

import java.util.Arrays;

public class CallInstruction extends Instruction<CallInstruction.CallExecutor> {
  public static final String INSTRUCTION = "call";
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallInstruction(String id, String instanceName, String methodName) {
    this(id, instanceName, methodName, new Object[]{});
  }

  public CallInstruction(String id, String instanceName, String methodName, Object[] args) {
    super(id);
    this.instanceName = instanceName;
    this.methodName = methodName;
    this.args = args;
  }

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

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("CallInstruction");
    sb.append("{id='").append(getId()).append('\'');
    sb.append(", instanceName='").append(instanceName).append('\'');
    sb.append(", methodName='").append(methodName).append('\'');
    sb.append(", args=").append(args == null ? "null" : Arrays.asList(args).toString());
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CallInstruction that = (CallInstruction) o;

    if (!Arrays.equals(args, that.args)) return false;
    if (!instanceName.equals(that.instanceName)) return false;
    if (!methodName.equals(that.methodName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + instanceName.hashCode();
    result = 31 * result + methodName.hashCode();
    result = 31 * result + Arrays.hashCode(args);
    return result;
  }
}
