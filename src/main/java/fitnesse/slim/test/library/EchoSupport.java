package fitnesse.slim.test.library;

public class EchoSupport {
  
  private boolean echoCalled;

  public void echo() {
    this.echoCalled = true;
  }
  
  public boolean echoSupportCalled() {
    return echoCalled;
  }
}
