package fitnesse.slim;

import java.util.Stack;

public class SlimHelperLibrary implements StatementExecutorConsumer {
  private static final String ACTOR_INSTANCE_NAME = "scriptTableActor";
  private StatementExecutor statementExecutor;
  private Stack<Object> actorStack = new Stack<Object>();

  public Object scriptTableActor() {
    return statementExecutor.getInstance(ACTOR_INSTANCE_NAME);
  }

  public void setStatementExecutor(StatementExecutor statementExecutor) {
    this.statementExecutor = statementExecutor;
  }

  public StatementExecutor getStatementExecutor() {
    return statementExecutor;
  }

  public void pushActor() {
    actorStack.push(scriptTableActor());
  }

  public void popActor() {
    Object actor = actorStack.pop();
    statementExecutor.setInstance(ACTOR_INSTANCE_NAME, actor);
  }
}
