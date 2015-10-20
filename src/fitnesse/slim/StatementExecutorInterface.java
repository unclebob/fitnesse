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
  Object getSymbol(String symbolName);
	
  Object getInstance(String instanceName);

  boolean stopHasBeenRequested();

  void reset();

  void setInstance(String actorInstanceName, Object actor);
}