package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

import java.util.Arrays;
import java.util.Optional;

abstract class CallAndOptionalAssignInstruction extends Instruction {
  private final String instructionName;
  private Optional<String> symbolName;
  private String instanceName;
  private String methodName;
  private Object[] args;

  protected CallAndOptionalAssignInstruction(String instructionName, String id, Optional<String> symbolName, String instanceName, String methodName, Object[] args) {
    super(id);
    this.instructionName = instructionName;
    this.symbolName = symbolName;
    this.instanceName = instanceName;
    this.methodName = methodName;
    this.args = args;
  }

  @Override
  protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
    Object result;
    if (symbolName.isPresent()) {
      result = executor.callAndAssign(symbolName.get(), instanceName, methodName, args);
    } else {
      result = executor.call(this.instanceName, this.methodName, this.args);
    }
    return new InstructionResult(getId(), result);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("{id='").append(getId()).append('\'');
    sb.append(", instruction='").append(instructionName).append('\'');
    symbolName.ifPresent(sn -> sb.append(", symbolName='").append(sn).append('\''));
    sb.append(", instanceName='").append(instanceName).append('\'');
    sb.append(", methodName='").append(methodName).append('\'');
    sb.append(", args=").append(Arrays.toString(args));
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CallAndOptionalAssignInstruction that = (CallAndOptionalAssignInstruction) o;

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
