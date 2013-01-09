package fitnesse.slim.instructions;

import static util.ListUtility.list;

public class ImportInstruction implements Instruction<ImportInstruction.ImportExecutor> {
  public static final String INSTRUCTION = "import";
  private String id;
  private String path;

  public ImportInstruction(String id, String path) {
    this.id = id;
    this.path = path;
  }

  @Override
  public Object execute(ImportExecutor executor) {
    Object result = executor.addPath(this.path);
    return list(id, result);
  }

  public static interface ImportExecutor extends InstructionExecutor {
    Object addPath(String path);
  }
}
