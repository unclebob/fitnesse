package fitnesse.slim.test.library;

public class SpecialEchoSupport {
  
  private boolean echoCalled;

  public void echo() {
    this.echoCalled = true;
  }
  
  public boolean specialEchoSupportCalled() {
    return echoCalled;
  }
}
