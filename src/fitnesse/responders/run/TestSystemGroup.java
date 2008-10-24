package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.slimResponder.SlimTestSystem;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.HashMap;
import java.util.Map;

public class TestSystemGroup {
  private Map<String, TestSystem> testSystems = new HashMap<String, TestSystem>();
  private FitNesseContext context;
  private WikiPage page;
  private TestSystemListener testSystemListener;
  private CompositeExecutionLog log;

  public TestSystemGroup(FitNesseContext context, WikiPage page, TestSystemListener listener) throws Exception {
    this.context = context;
    this.page = page;
    this.testSystemListener = listener;
    log = new CompositeExecutionLog(page);
  }

  public void add(String testSystemName, TestSystem testSystem) {
    testSystems.put(testSystemName, testSystem);
  }

  public CompositeExecutionLog getExecutionLog() throws Exception {
    return log;
  }

  public void bye() throws Exception {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.bye();
    }
  }

  public void kill() throws Exception {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.kill();
    }
  }

  public void setFastTest(boolean fastTest) {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.setFastTest(fastTest);
    }
  }

  public boolean isSuccessfullyStarted() {
    for (TestSystem testSystem : testSystems.values())
      if (testSystem.isSuccessfullyStarted() == false)
        return false;
    return true;
  }

  TestSystem startTestSystem(String testSystemName, String testRunner, String classPath) throws Exception {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(testSystemName)) {
      testSystem = makeTestSystem(testSystemName);
      testSystems.put(testSystemName, testSystem);
      log.add(testSystemName, testSystem.getExecutionLog(classPath, testRunner));
      testSystem.start();
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(String testSystemName) throws Exception {
    if ("slim".equalsIgnoreCase(TestSystem.getTestSystemType(testSystemName)))
      return new SlimTestSystem(page, testSystemListener);
    else
      return new FitTestSystem(context, page, testSystemListener);
  }

}
