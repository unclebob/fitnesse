/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.util.ArrayList;
import java.util.List;

import fit.Counts;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.responders.run.slimResponder.HtmlSlimTestSystem;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class SlimTestEngine implements TestEngine {
  private class DummyPage extends InMemoryPage {
    public DummyPage(String testName, String content) throws Exception {
      super(testName, content, null);
    }
  }

  private class CountingListener implements TestSystemListener {
    public void acceptOutputFirst(String output) throws Exception {
    }

    public void acceptResultsLast(TestSummary testSummary) throws Exception {
    }

    public void exceptionOccurred(Throwable e) {
    }

    public void testComplete(TestSummary testSummary) throws Exception {
    }
  }

  public TestResult runTest(final TestDescriptor test) {
    if (!test.getContent().contains("<table")) {
      return new SingleTestResult(new Counts(), test.getName(),
          " contains no tables");
    }
    try {
      WikiPage wp = new DummyPage(test.getName(), test.getContent());
      PageData pd = new PageData(wp) {
        @Override
        public String getHtml() throws Exception {
          return test.getContent();
        }

        @Override
        public List<String> getClasspaths() throws Exception {
          return new ArrayList<String>();
        }
      };
      CountingListener listener = new CountingListener();
      HtmlSlimTestSystem slim = new HtmlSlimTestSystem(wp, listener);
      TestSystem.Descriptor descriptor = TestSystem.getDescriptor(wp.getData(),
          false);
      descriptor.testRunner = "fitnesse.slim.SlimService";

      slim.setFastTest(true);
      slim.getExecutionLog("", descriptor);
      slim.start();

      String html = slim.runTestsAndGenerateHtml(pd);
      slim.bye();
      TestSummary counters = slim.getTestSummary();
      return new SingleTestResult(new Counts(counters.right, counters.wrong,
          counters.ignores, counters.exceptions), test.getName(), html);
    } catch (Exception e) {
      Counts c = new Counts();
      c.exceptions = 1;
      e.printStackTrace();
      return new SingleTestResult(c, test.getName(), e.toString() + "\n");
    }
  }
}
