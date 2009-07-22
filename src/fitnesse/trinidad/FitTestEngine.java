/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.PrintWriter;
import java.io.StringWriter;

import fit.Counts;
import fit.Fixture;
import fit.Parse;

public class FitTestEngine implements TestEngine {

  public TestResult runTest(TestDescriptor test) {
    if (!test.getContent().contains("<table")) {
      return new SingleTestResult(new Counts(), test.getName(),
          " contains no tables");
    }
    try {
      Parse tables = new Parse(test.getContent());
      Fixture f = new Fixture();
      SimpleCounter pdl = new SimpleCounter();
      f.listener = pdl;
      f.doTables(tables);

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      tables.print(pw);
      pw.flush();

      return new SingleTestResult(pdl.getCounts(), test.getName(), sw
          .getBuffer().toString());
    } catch (Exception e) {
      Counts c = new Counts();
      c.exceptions = 1;
      return new SingleTestResult(c, test.getName(), e.toString());
    }
  }
}
