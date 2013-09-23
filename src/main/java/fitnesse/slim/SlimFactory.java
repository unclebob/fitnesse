package fitnesse.slim;

import fitnesse.slim.StatementExecutorInterface;

public abstract class SlimFactory {

  public abstract NameTranslator getMethodNameTranslator();

  public SlimServer getSlimServer(boolean verbose) {
    return new SlimServer(verbose, this);
  }

  public ListExecutor getListExecutor(boolean verbose) {
    return new ListExecutor(verbose, this);
  }

  public abstract StatementExecutorInterface getStatementExecutor();
  
  public void stop() {
  }

}
