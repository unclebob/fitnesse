package fitnesse.slim;

import fitnesse.slim.instructions.InstructionExecutor;

public interface StatementExecutorInterface extends InstructionExecutor {

  /*
   * This method can be used by TableTable custom fixtures to have access
   * to the table of symbols. This enables elaborate fixtures that can
   * both assign and resolve any symbols on their own.
   * 
   * Have a look to this FitNesse page for some examples:
   * FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TableTableSuite.TestTableTableImplementingStatementExecutorConsumer
   */
  public abstract Object getSymbol(String symbolName);
	
  public abstract Object getInstance(String instanceName);

  public abstract boolean stopHasBeenRequested();

  public abstract void reset();

  public abstract void setInstance(String actorInstanceName, Object actor);
}