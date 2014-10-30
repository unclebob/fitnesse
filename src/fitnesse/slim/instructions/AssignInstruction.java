package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

public class AssignInstruction extends Instruction {
  public static final String INSTRUCTION = "assign";
  private String symbolName;
  private Object value;

  public AssignInstruction(String id, String symbolName, Object value) {
    super(id);
    this.symbolName = symbolName;
    this.value = value;
  }

  @Override
  protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
    executor.assign(symbolName, value);
    return new InstructionResult.Ok(getId());
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("{id='").append(getId()).append('\'');
    sb.append(", instruction='").append(INSTRUCTION).append('\'');
    sb.append(", symbolName='").append(symbolName).append('\'');
    sb.append(", value='").append(value).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    AssignInstruction that = (AssignInstruction) o;

    if (!value.equals(that.value)) return false;
    return symbolName.equals(that.symbolName);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + symbolName.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
