package fitnesse.responders.run;

import fitnesse.wiki.WikiPage;


/**
 * This is the listener used to format the out of the MultipleTestsRunner
 * 
 * @author Clare McLennan
 */
public interface ResultsListener {
  
  public void announceStartTestSystem(String testSystemName, String testRunner) throws Exception;
  public void setExecutionLog(CompositeExecutionLog log);

  public void announceStartNewTest(WikiPage test) throws Exception;

  public void processTestOutput(String output) throws Exception;

  public void processTestResults(WikiPage test, TestSummary testSummary) throws Exception;
  
  public void errorOccured();
}
