/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */
package fitnesse.trinidad.examples;

import fitnesse.trinidad.*;

import fit.Counts;

public class InProcessRunner {
  public static void main(String[] args) throws Exception {
    // TestRunner tdd=new TestRunner(new
    // FitNesseRepository("src/test/resources"),
    // new FitTestEngine(),"/tmp/fitnesse");
    TestRunner tdd = new TestRunner(new FitNesseRepository("."),
        new SlimTestEngine(), "/tmp/fitnesse/slim");

    Counts cs = tdd
        .runTest("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SlimSymbolsCanBeBlankOrNull");
    System.err.println("Total right=" + cs.right + "; wrong=" + cs.wrong
        + " exceptions=" + cs.exceptions);
  }
}
