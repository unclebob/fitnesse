package fitnesse.testutil;

import fit.Fixture;
import fit.Parse;

public class WaitFixture  {

  public WaitFixture() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
