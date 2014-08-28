package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimException;

import java.util.Arrays;

public class CallAndAssignInstruction extends Instruction {
  public static final String INSTRUCTION = "callAndAssign";
  private String symbolName;
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName) {
    this(id, symbolName, instanceName, methodName, new Object[]{});
  }

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName, Object[] args) {
    super(id);
    this.symbolName = symbolName;
    this.instanceName = instanceName;
    this.methodName = methodName;
    this.args = args;
  }

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName, Object[] args,
                                  NameTranslator methodNameTranslator) {
    super(id);
    this.symbolName = symbolName;
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
    Object result = executor.callAndAssign(symbolName, instanceName, methodName, args);
    return new InstructionResult(getId(), result);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("{id='").append(getId()).append('\'');
    sb.append(", instruction='").append(INSTRUCTION).append('\'');
    sb.append(", symbolName='").append(symbolName).append('\'');
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

    CallAndAssignInstruction that = (CallAndAssignInstruction) o;

    if (!Arrays.equals(args, that.args)) return false;
    if (!instanceName.equals(that.instanceName)) return false;
    if (!methodName.equals(that.methodName)) return false;
    return symbolName.equals(that.symbolName);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + symbolName.hashCode();
    result = 31 * result + instanceName.hashCode();
    result = 31 * result + methodName.hashCode();
    result = 31 * result + Arrays.hashCode(args);
    return result;
  }
}
