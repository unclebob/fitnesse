package fitnesse.testutil;


public class WaitFixture  {

  public WaitFixture() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
