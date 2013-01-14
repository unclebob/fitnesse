package fitnesse.slim;

import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.ImportInstruction;
import fitnesse.slim.instructions.MakeInstruction;

public interface StatementExecutorInterface
    extends CallAndAssignInstruction.CallAndAssignExecutor, CallInstruction.CallExecutor,
    ImportInstruction.ImportExecutor, MakeInstruction.MakeExecutor {

  public abstract void setVariable(String name, Object value);

  public abstract Object getInstance(String instanceName);

  public abstract boolean stopHasBeenRequested();

  public abstract void reset();

  public abstract void setInstance(String actorInstanceName, Object actor);
}