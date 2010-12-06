package fitnesse.slim;

public interface StatementExecutorInterface {

  public abstract void setVariable(String name, Object value);

  public abstract Object addPath(String path);

  public abstract Object getInstance(String instanceName);

  public abstract Object create(String instanceName, String className, Object[] args);

  public abstract Object call(String instanceName, String methodName, Object... args);

  public abstract boolean stopHasBeenRequested();

  public abstract void reset();

  public abstract Object callAndAssign(String variable, String instanceName, String methodName,
      Object[] args);

}