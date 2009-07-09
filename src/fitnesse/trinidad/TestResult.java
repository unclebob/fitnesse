/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import fit.Counts;

public interface TestResult {
  Counts getCounts();

  String getName();

  String getContent();
}
