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
}
