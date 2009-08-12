package fitnesse.slim;

public class JavaSlimFactory extends SlimFactory {

  public StatementExecutorInterface getStatementExecutor() throws Exception {
    return new StatementExecutor();
  }
}
