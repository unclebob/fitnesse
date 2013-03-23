package fitnesse.slim;

import fitnesse.slim.instructions.InstructionExecutor;

public interface StatementExecutorInterface extends InstructionExecutor {

  public abstract void setVariable(String name, Object value);

  public abstract Object getInstance(String instanceName);

  public abstract boolean stopHasBeenRequested();

  public abstract void reset();

  public abstract void setInstance(String actorInstanceName, Object actor);
}