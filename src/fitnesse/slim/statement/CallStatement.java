package fitnesse.slim.statement;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.StatementExecutorInterface;

import static util.ListUtility.list;

public class CallStatement implements Statement {
  public static final String INSTRUCTION = "call";
  private String id;
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallStatement(String id, String instanceName, String methodName, Object[] args, NameTranslator methodNameTranslator) {
    this.id = id;
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  public Object execute(StatementExecutorInterface executor) {
    Object result = executor.call(this.instanceName, this.methodName, this.args);
    return list(id, result);
  }
}
