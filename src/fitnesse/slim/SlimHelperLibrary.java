package fitnesse.slim;

import java.util.Stack;

public class SlimHelperLibrary implements StatementExecutorConsumer {
  private static final String ACTOR_INSTANCE_NAME = "scriptTableActor";
  private StatementExecutor statementExecutor;
  private Stack<Object> fixtureStack = new Stack<Object>();

  public Object getFixture() {
    return statementExecutor.getInstance(ACTOR_INSTANCE_NAME);
  }

  public void setStatementExecutor(StatementExecutor statementExecutor) {
    this.statementExecutor = statementExecutor;
  }

  public StatementExecutor getStatementExecutor() {
    return statementExecutor;
  }

  public void pushFixture() {
    fixtureStack.push(getFixture());
  }

  public void popFixture() {
    Object actor = fixtureStack.pop();
    statementExecutor.setInstance(ACTOR_INSTANCE_NAME, actor);
  }
}
