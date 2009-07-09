/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import fit.Counts;
import fit.FixtureListener;
import fit.Parse;

public class SimpleCounter implements FixtureListener {
  private Counts counts = new Counts();

  public SimpleCounter() {
  }

  public void tableFinished(Parse table) {
  }

  public void tablesFinished(Counts count) {
    counts.tally(count);
  }

  public Counts getCounts() {
    return counts;
  }
}