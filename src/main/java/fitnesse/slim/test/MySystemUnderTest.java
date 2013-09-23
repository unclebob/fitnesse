package fitnesse.slim.test;

public class MySystemUnderTest {
  private boolean called;

  public void bar() {
    called = true;
  }
  
  
  public boolean systemUnderTestCalled() {
    return called;
  }
  
}
