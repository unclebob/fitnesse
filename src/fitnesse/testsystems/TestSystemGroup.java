// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.wiki.WikiPage;

public class TestSystemGroup {
  private Map<TestSystem.Descriptor, TestSystem> testSystems = new HashMap<TestSystem.Descriptor, TestSystem>();
  private FitNesseContext context;
  private WikiPage page;
  private TestSystemListener testSystemListener;
  private CompositeExecutionLog log;
  private boolean fastTest = false;
  private boolean manualStart = false;

  public TestSystemGroup(FitNesseContext context, WikiPage page, TestSystemListener listener) {
    this.context = context;
    this.page = page;
    this.testSystemListener = listener;
    log = new CompositeExecutionLog(page);
  }

  public CompositeExecutionLog getExecutionLog() {
    return log;
  }

  public void kill() throws IOException {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.kill();
    }
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void setManualStart(boolean manualStart) {
    this.manualStart = manualStart;
  }

  public boolean isSuccessfullyStarted() {
    for (TestSystem testSystem : testSystems.values())
      if (testSystem.isSuccessfullyStarted() == false)
        return false;
    return true;
  }

  public TestSystem startTestSystem(TestSystem.Descriptor descriptor, String classPath) throws IOException {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(descriptor)) {
      testSystem = makeTestSystem(new TestSystem.Descriptor(descriptor, classPath));
      testSystem.setFastTest(fastTest);
      testSystem.setManualStart(manualStart);
      testSystems.put(descriptor, testSystem);

      // TODO: Need to pass classPath to test system.
      log.add(descriptor.getTestSystemName(), testSystem.getExecutionLog());
      testSystem.start();
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(TestSystem.Descriptor descriptor) {
    if ("slim".equalsIgnoreCase(TestSystem.getTestSystemType(descriptor.getTestSystemName())))
      return new HtmlSlimTestSystem(page, descriptor, testSystemListener);
    else
      return new FitTestSystem(context, page, descriptor, testSystemListener);
  }

}
