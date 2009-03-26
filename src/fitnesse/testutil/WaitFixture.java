package fitnesse.testutil;

import fit.Fixture;
import fit.Parse;

public class WaitFixture extends Fixture {
  @Override
  public void doTable(Parse table) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.doTable(table);
  }
}
