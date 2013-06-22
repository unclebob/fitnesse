// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.slim.SlimClient;
import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.wiki.WikiPage;

public class TestSystemGroup {
  private Map<Descriptor, TestSystem> testSystems = new HashMap<Descriptor, TestSystem>();
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

  public TestSystem startTestSystem(Descriptor descriptor, String classPath) throws IOException {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(descriptor)) {
      testSystem = makeTestSystem(new Descriptor(descriptor, classPath));

      log.add(descriptor.getTestSystemName(), testSystem.getExecutionLog());
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(Descriptor descriptor) throws IOException {
    if ("slim".equalsIgnoreCase(ClientBuilder.getTestSystemType(descriptor.getTestSystemName())))
      return createHtmlSlimTestSystem(descriptor);
    else
      return createFitTestSystem(descriptor);
  }

  private HtmlSlimTestSystem createHtmlSlimTestSystem(Descriptor descriptor) throws IOException {
    SlimClientBuilder builder = new SlimClientBuilder(page, descriptor);
    builder.setFastTest(fastTest);
    builder.setManualStart(manualStart);
    builder.start();
    SlimClient slimClient = builder.getSlimClient();

    HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(slimClient, testSystemListener, new ExecutionLog(page, slimClient.getTestRunner()));

    testSystems.put(descriptor, testSystem);

    return testSystem;
  }

  private FitTestSystem createFitTestSystem(Descriptor descriptor) {
    FitTestSystem testSystem = new FitTestSystem(context, page, descriptor, testSystemListener);
    testSystem.setFastTest(fastTest);
    testSystem.setManualStart(manualStart);
    testSystems.put(descriptor, testSystem);

    testSystem.start();
    return testSystem;
  }

}
