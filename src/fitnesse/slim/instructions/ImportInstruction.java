package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

public class ImportInstruction extends Instruction<ImportInstruction.ImportExecutor> {
  public static final String INSTRUCTION = "import";
  private String path;

  public ImportInstruction(String id, String path) {
    super(id);
    this.path = path;
  }

  @Override
  protected Object executeInternal(ImportExecutor executor) throws SlimException {
    return executor.addPath(this.path);
  }

  public static interface ImportExecutor extends InstructionExecutor {
    Object addPath(String path) throws SlimException;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ImportInstruction that = (ImportInstruction) o;

    if (!path.equals(that.path)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + path.hashCode();
    return result;
  }
}
