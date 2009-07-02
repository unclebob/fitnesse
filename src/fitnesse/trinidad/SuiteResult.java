/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.util.ArrayList;
import java.util.List;

import fit.Counts;

public class SuiteResult implements TestResult {
  private String name;

  public SuiteResult(String name) {
    this.name = name;
    content = new StringBuffer(

    "<head><title>")
        .append(name)
        .append(
            "</title><link rel='stylesheet' type='text/css' href='fitnesse.css' media='screen'/>"
                + "<link rel='stylesheet' type='text/css' href='fitnesse_print.css' media='print'/>"
                + "</head><body><h2>")
        .append(name)
        .append(
            "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>");
  }

  private Counts counts = new Counts();

  private StringBuffer content;

  public String getContent() {
    return content + "</table></body></html>";
  }

  public Counts getCounts() {
    return counts;
  }

  public String getName() {
    return name;
  }

  private String getCssClass(Counts c) {
    if (c.exceptions > 0)
      return "error";
    if (c.wrong > 0)
      return "fail";
    if (c.right > 0)
      return "pass";
    return "plain";
  }

  private List<TestResult> testResults = new ArrayList<TestResult>();

  public void append(TestResult tr) {
    counts.tally(tr.getCounts());
    testResults.add(tr);
    content.append("<tr class='").append(getCssClass(tr.getCounts())).append(
        "'><td>").append("<a href=\"./").append(tr.getName())
        .append(".html\">").append(tr.getName()).append("</a>").append(
            "</td><td>").append(tr.getCounts().right).append("</td><td>")
        .append(tr.getCounts().wrong).append("</td><td>").append(
            tr.getCounts().exceptions).append("</td></tr>");
  }

  public List<TestResult> getTestResults() {
    return testResults;
  }
}
