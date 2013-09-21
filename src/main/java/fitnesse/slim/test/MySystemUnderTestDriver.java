package fitnesse.slim.test;

import fitnesse.slim.SystemUnderTest;

public class MySystemUnderTestDriver {

  @SystemUnderTest
  public MySystemUnderTest systemUnderTest = new MySystemUnderTest();

  private boolean called;

  public void foo() {
    called = true;
  }

  public boolean driverCalled() {
    return called;
  }
}
