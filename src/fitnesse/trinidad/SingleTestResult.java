/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import fit.Counts;

public class SingleTestResult implements TestResult {
  private Counts counts;
  private String name;
  private String content;

  public Counts getCounts() {
    return counts;
  }

  public String getName() {
    return name;
  }

  public String getContent() {
    return content;
  }

  public SingleTestResult(Counts counts, String name, String content) {
    super();
    this.counts = counts;
    this.name = name;
    this.content = content;
  }

}
