package fitnesse.testsystems;

import java.util.LinkedList;
import java.util.List;

public class CompositeExecutionLogListener implements ExecutionLogListener {

  private final List<ExecutionLogListener> listeners = new LinkedList<>();

  public final void addExecutionLogListener(ExecutionLogListener listener) {
    listeners.add(listener);
  }

  protected final List<ExecutionLogListener> listeners() {
    return listeners;
  }

  @Override
  public void commandStarted(ExecutionContext context) {
    for (ExecutionLogListener listener : listeners)
      listener.commandStarted(context);
  }

  @Override
  public void stdOut(String output) {
    for (ExecutionLogListener listener : listeners)
      listener.stdOut(output);
  }

  @Override
  public void stdErr(String output) {
    for (ExecutionLogListener listener : listeners)
      listener.stdErr(output);
  }

  @Override
  public void exitCode(int exitCode) {
    for (ExecutionLogListener listener : listeners)
      listener.exitCode(exitCode);
  }

  @Override
  public void exceptionOccurred(Throwable e) {
    for (ExecutionLogListener listener : listeners)
      listener.exceptionOccurred(e);
  }
}
