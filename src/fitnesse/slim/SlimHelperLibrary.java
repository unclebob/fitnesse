package fitnesse.slim;

import java.util.Stack;

public class SlimHelperLibrary implements StatementExecutorConsumer {
  private static final String ACTOR_INSTANCE_NAME = "scriptTableActor";
  private StatementExecutorInterface statementExecutor;
  private Stack<Object> fixtureStack = new Stack<>();

  public Object getFixture() {
    return statementExecutor.getInstance(ACTOR_INSTANCE_NAME);
  }

  @Override
  public void setStatementExecutor(StatementExecutorInterface statementExecutor) {
    this.statementExecutor = statementExecutor;
  }

  public StatementExecutorInterface getStatementExecutor() {
    return statementExecutor;
  }

  public void pushFixture() {
    fixtureStack.push(getFixture());
  }

  public void popFixture() {
    Object actor = fixtureStack.pop();
    statementExecutor.setInstance(ACTOR_INSTANCE_NAME, actor);
  }

  // The following functions are used to manipulate Symbols from the Slim Tables
  public Object cloneSymbol(Object master){
	  return master;
  }
}
