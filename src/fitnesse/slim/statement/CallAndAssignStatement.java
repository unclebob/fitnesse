package fitnesse.slim.statement;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.StatementExecutorInterface;

import static util.ListUtility.list;

/**
 * TODO Document class.
 */
public class CallAndAssignStatement implements Statement {
  public static final String INSTRUCTION = "callAndAssign";
  private String id;
  private String symbolName;
  private String instanceName;
  private String methodName;
  private Object[] args;

  public CallAndAssignStatement(String id, String symbolName, String instanceName, String methodName, Object[] args, NameTranslator methodNameTranslator) {
    this.id = id;
    this.symbolName = symbolName;
    this.instanceName = instanceName;
    this.methodName = methodNameTranslator.translate(methodName);
    this.args = args;
  }

  @Override
  public Object execute(StatementExecutorInterface executor) {
    Object result = executor.callAndAssign(symbolName, instanceName, methodName, args);
    return list(id, result);
  }
}
