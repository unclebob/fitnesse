// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.slimResponder.HtmlSlimTestSystem;
import fitnesse.wiki.WikiPage;

public class TestSystemGroup {
  private Map<TestSystem.Descriptor, TestSystem> testSystems = new HashMap<TestSystem.Descriptor, TestSystem>();
  private FitNesseContext context;
  private WikiPage page;
  private TestSystemListener testSystemListener;
  private CompositeExecutionLog log;
  private boolean fastTest = false;

  public TestSystemGroup(FitNesseContext context, WikiPage page, TestSystemListener listener) throws Exception {
    this.context = context;
    this.page = page;
    this.testSystemListener = listener;
    log = new CompositeExecutionLog(page);
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
    this.fastTest = fastTest;
  }

  public boolean isSuccessfullyStarted() {
    for (TestSystem testSystem : testSystems.values())
      if (testSystem.isSuccessfullyStarted() == false)
        return false;
    return true;
  }

  TestSystem startTestSystem(TestSystem.Descriptor descriptor, String classPath) throws Exception {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(descriptor)) {
      testSystem = makeTestSystem(descriptor);
      testSystem.setFastTest(fastTest);
      testSystems.put(descriptor, testSystem);
      log.add(descriptor.testSystemName, testSystem.getExecutionLog(classPath, descriptor));
      testSystem.start();
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(TestSystem.Descriptor descriptor) throws Exception {
    if ("slim".equalsIgnoreCase(TestSystem.getTestSystemType(descriptor.testSystemName)))
      return new HtmlSlimTestSystem(page, testSystemListener);
    else
      return new FitTestSystem(context, page, testSystemListener);
  }

}
