package fitnesse.slim;

import fitnesse.slim.StatementExecutorInterface;

public abstract class SlimFactory {

  public SlimServer getSlimServer(boolean verbose) {
    return new SlimServer(verbose, this);
  }

  public ListExecutor getListExecutor(boolean verbose) throws Exception {
    return new ListExecutor(verbose, this.getStatementExecutor());
  }

  public abstract StatementExecutorInterface getStatementExecutor()
      throws Exception;
  
  public void stop() {
  }

}
