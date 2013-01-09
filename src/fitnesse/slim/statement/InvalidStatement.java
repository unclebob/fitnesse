package fitnesse.slim.statement;

import fitnesse.slim.SlimServer;
import fitnesse.slim.StatementExecutorInterface;

import static java.lang.String.format;
import static util.ListUtility.list;

public class InvalidStatement implements Statement {
  private final String id;
  private final String operation;

  public InvalidStatement(String id, String operation) {
    this.id = id;
    this.operation = operation;
  }

  @Override
  public Object execute(StatementExecutorInterface executor) {
    return list(id, format("%smessage:<<INVALID_STATEMENT: %s.>>", SlimServer.EXCEPTION_TAG, operation));
  }
}
