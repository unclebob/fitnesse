package fitnesse.fixtures;

import fit.ColumnFixture;

public class SystemExitTable  extends ColumnFixture {

  private int exitCode;
  
  private Throwable exception;
  
  public void setSystemExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  // slim:
  public void execute() {
    try {
      System.exit(exitCode);
    } catch (Throwable e) {
      exception = e;
    }
  }

  // fit:
  public boolean valid() throws Exception {
    exitSystem(exitCode);
    return true;
  }

  public String exceptionMessage() {
    return exception.getMessage();
  }
  
  public void exitSystem(int code) {
    System.exit(code);
  }
}
