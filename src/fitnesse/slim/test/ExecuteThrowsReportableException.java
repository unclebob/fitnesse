package fitnesse.slim.test;

public class ExecuteThrowsReportableException {
  public void setX(int x){

  }
  public void execute() {
    throw new RuntimeException("A Reportable Exception");
  }
}
