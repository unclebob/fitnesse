package fitnesse.slim.statement;

import fitnesse.slim.StatementExecutorInterface;

import static util.ListUtility.list;

public class ImportStatement implements Statement {
  public static final String INSTRUCTION = "import";
  private String id;
  private String path;

  public ImportStatement(String id, String path) {
    this.id = id;
    this.path = path;
  }

  @Override
  public Object execute(StatementExecutorInterface executor) {
    Object result = executor.addPath(this.path);
    return list(id, result);
  }
}
