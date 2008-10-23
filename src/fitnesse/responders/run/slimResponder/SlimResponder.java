package fitnesse.responders.run.slimResponder;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.responders.run.TestSystemBase;
import fitnesse.wiki.PageData;

/*
This responder is a test rig for SlimResponderTest, which makes sure that the SlimTestSystem works nicely with
responders in general.
*/
public class SlimResponder extends WikiPageResponder implements TestSystemListener {
  private boolean slimOpen = false;
  ExecutionLog log;
  private boolean fastTest = false;
  SlimTestSystem testSystem;


  protected void processWikiPageDataBeforeGeneratingHtml(PageData pageData) throws Exception {
    testSystem = new SlimTestSystem(pageData.getWikiPage(), this);
    String classPath = new ClassPathBuilder().getClasspath(page);
    log = testSystem.getExecutionLog(classPath, "fitnesse.slim.SlimService");
    testSystem.start();
    testSystem.setFastTest(fastTest);
    testSystem.sendPageData(pageData);
    testSystem.bye();
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  boolean slimOpen() {
    return slimOpen;
  }

  public PageData getTestResults() {
    return testSystem.getTestResults();
  }

  protected void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void acceptOutput(String output) throws Exception {
  }

  public void acceptResults(TestSystemBase.TestSummary testSummary) throws Exception {
  }

  public void exceptionOccurred(Throwable e) {
  }
}

