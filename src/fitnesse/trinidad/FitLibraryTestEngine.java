/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.PrintWriter;
import java.io.StringWriter;

import fit.Counts;
import fit.Parse;
import fitlibrary.suite.FitLibraryServer;

public class FitLibraryTestEngine implements TestEngine {

  public TestResult runTest(TestDescriptor test) {
    if (!test.getContent().contains("<table")) {
      return new SingleTestResult(new Counts(), test.getName(),
          " contains no tables");
    }
    try {
      Parse tables = new Parse(test.getContent());
      FitLibraryServer fls = new FitLibraryServer();
      SimpleCounter pdl = new SimpleCounter();
      fls.fixtureListener = pdl;
      fls.doTables(tables);

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
