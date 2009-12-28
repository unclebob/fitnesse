package fitnesse.junit;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class JUnitRunNotifierResultsListener implements ResultsListener{
    private final Class<?> mainClass;
    private final RunNotifier notifier;
    public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass){
      this.notifier = notifier;
      this.mainClass = mainClass;
    }
    @Override
    public void allTestingComplete() throws Exception {

    }
    @Override
    public void announceNumberTestsToRun(int testsToRun) {
      
    }
    @Override
    public void errorOccured() {
      
    }
    @Override
    public void newTestStarted(WikiPage test, long time) throws Exception {
      notifier.fireTestStarted(descriptionFor(test));
    }
    private Description descriptionFor(WikiPage test) throws Exception {
      return Description.createTestDescription(mainClass, new WikiPagePath(test).toString());
    }
    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log)
        throws Exception {
    }
    @Override
    public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
      if (testSummary.wrong == 0 && testSummary.exceptions == 0) {
        notifier.fireTestFinished(descriptionFor(test));
      } else {
        notifier.fireTestFailure(new Failure(descriptionFor(test), new AssertionError(
            "wrong: " + testSummary.wrong + " exceptions: " + testSummary.exceptions)));
      }
    }
    @Override
    public void testOutputChunk(String output) throws Exception {

    }
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
        throws Exception {
    }    
  }

