package fitnesse.slim.statement;

import fitnesse.slim.StatementExecutorInterface;

import static util.ListUtility.list;

/**
 * TODO Document class.
 */
public class MakeStatement implements Statement {
  public static final String INSTRUCTION = "make";
  private final String id;
  private final String instanceName;
  private final String className;
  private final Object[] args;

  public MakeStatement(String id, String instanceName, String className, Object[] args) {
    this.id = id;
    this.instanceName = instanceName;
    this.className = className;
    this.args = args;
  }


  @Override
  public Object execute(StatementExecutorInterface executor) {
    Object instance = executor.create(instanceName, className, args);
    return list(id, instance);
  }
}
