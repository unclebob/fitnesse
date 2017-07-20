package fit.testFxtr;

import fit.Fixture;

public class TestActionFixture extends Fixture {
  public boolean checked = false;
  public int entered = -1;
  public boolean buttonPressed= false;

  public int data() {
    checked = true;
    return 42;
  }

  public void data(int x) {
    entered = x;
  }

  public void button() {
    buttonPressed = true;
  }

  public Unadapted unadaptable() {
    return new Unadapted();
  }

  public class Unadapted {
  }

  public void overload(int i) {

  }

  public void overload(double d) {

  }
}
