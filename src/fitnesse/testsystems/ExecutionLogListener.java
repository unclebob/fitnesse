package fitnesse.testsystems;

public interface ExecutionLogListener {

  void commandStarted(ExecutionContext context);

  void stdOut(String output);

  void stdErr(String output);

  void exitCode(int exitCode);

  void exceptionOccurred(Throwable e);

  interface ExecutionContext {

    String getCommand();

    String getTestSystemName();
  }
}
