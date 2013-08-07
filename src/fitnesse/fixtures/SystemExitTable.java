package fitnesse.fixtures;

public class SystemExitTable {

  private int exitCode;
  
  private Throwable exception;
  
  public void setSystemExitCode(int exitCode) {
    this.exitCode = exitCode;
  }
  
  public void execute() {
    try {
      System.exit(exitCode);
    } catch (Throwable e) {
      exception = e;
    }
  }
  
  public String exceptionMessage() {
    return exception.getMessage();
  }
  
  public void exitSystem(int code) {
    System.exit(code);
  }
}
