package fitnesse.slim;

public abstract class SlimFactory {

  public abstract NameTranslator getMethodNameTranslator();

  public abstract boolean isVerbose();

  public SlimServer getSlimServer() {
    return new SlimServer(this);
  }

  public ListExecutor getListExecutor() {
    return new ListExecutor(isVerbose(), this);
  }

  public abstract StatementExecutorInterface getStatementExecutor();
  
  public void stop() {
  }

}
