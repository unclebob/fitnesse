package fitnesse.slim;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Jsr223StatementExecutor implements StatementExecutorInterface{
  protected Jsr223Bridge bridge;
  private Object statementExecutorProxy;
  
  public Jsr223StatementExecutor(Jsr223Bridge bridge) throws Exception
  {
    this.bridge = bridge;
    statementExecutorProxy = bridge.getStatementExecutor();
  }
  
  protected Object getStatementExecutorProxy()
  {
    return statementExecutorProxy;
  }
  
  public Object addPath(String path) {
    return callMethod("addPath", new Object[] {path});
  }

  public Object call(String instanceName, String methodName, Object... args) {
    return callMethod("call", new Object[] {instanceName, methodName, args});
  }

  public Object create(String instanceName, String className, Object[] args) {
    return callMethod("create", new Object[] {instanceName, className, args});
  }

  public Object getInstance(String instanceName) {
    return callMethod("getInstance", new Object[] {instanceName});
  }

  public void setVariable(String name, Object value) {
    callMethod("setVariable", new Object[] {name, value});
  }

  public boolean stopHasBeenRequested() {
    return (Boolean) callMethod("stopHasBeenRequested");
  }

  public void reset() {
    callMethod("reset");
  }

  protected Object callMethod(String method, Object... args) {
    try {
      return bridge.invokeMethod(getStatementExecutorProxy(), method, args);
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private String exceptionToString(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    exception.printStackTrace(pw);
    return SlimServer.EXCEPTION_TAG + stringWriter.toString();
  }
}
