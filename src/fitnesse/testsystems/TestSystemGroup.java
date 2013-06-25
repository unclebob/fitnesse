// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.slim.SlimCommandRunningClient;
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
  private boolean remoteDebug;

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

  public void setRemoteDebug(boolean remoteDebug) {
    this.remoteDebug = remoteDebug;
  }

  public TestSystem startTestSystem(Descriptor descriptor, String classPath) throws IOException {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(descriptor)) {
      testSystem = makeTestSystem(descriptor, classPath);

      testSystems.put(descriptor, testSystem);
      testSystem.start();

      log.add(descriptor.getTestSystemName(), testSystem.getExecutionLog());
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(Descriptor descriptor, String classPath) throws IOException {
    if ("slim".equalsIgnoreCase(ClientBuilder.getTestSystemType(descriptor.getTestSystemName())))
      return createHtmlSlimTestSystem(descriptor.getTestSystem(), classPath);
    else
      return createFitTestSystem(descriptor.getTestSystem(), classPath);
  }

  private HtmlSlimTestSystem createHtmlSlimTestSystem(String testSystemName, String classPath) throws IOException {
    SlimCommandRunningClient slimClient = new SlimClientBuilder(page.getData(), classPath)
            .withFastTest(fastTest)
            .withManualStart(manualStart)
            .withRemoteDebug(remoteDebug)
            .build();

    HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(testSystemName, slimClient, testSystemListener, new ExecutionLog(page, slimClient.getCommandRunner()));

    return testSystem;
  }

  private FitTestSystem createFitTestSystem(String testSystemName, String classPath) throws IOException {
    FitTestSystem testSystem = new FitTestSystem(testSystemName, context, page, classPath, testSystemListener);
    testSystem.withFastTest(fastTest)
            .withManualStart(manualStart)
            .withRemoteDebug(remoteDebug)
            .build();

    return testSystem;
  }

}
