package fitnesse.testsystems;

public class ConsoleExecutionLogListener implements ExecutionLogListener {
  @Override
  public void commandStarted(ExecutionContext context) {
	    System.out.println(context.getTestSystemName());
	    System.out.println(context.getCommand());
  }

  @Override
  public void stdOut(String output) {
    System.out.println(output);
  }

  @Override
  public void stdErr(String output) {
    System.err.println(output);
  }

  @Override
  public void exitCode(int exitCode) {
  }

  @Override
  public void exceptionOccurred(Throwable e) {
    e.printStackTrace();
  }
}
