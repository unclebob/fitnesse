package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

public class ImportInstruction extends Instruction {
  public static final String INSTRUCTION = "import";
  private String path;

  public ImportInstruction(String id, String path) {
    super(id);
    this.path = path;
  }

  @Override
  protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
    executor.addPath(this.path);
    return new InstructionResult.Ok(getId());
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("{id='").append(getId()).append('\'');
    sb.append(", instruction='").append(INSTRUCTION).append('\'');
    sb.append(", path='").append(path).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ImportInstruction that = (ImportInstruction) o;

    return path.equals(that.path);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + path.hashCode();
    return result;
  }
}
