// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.fit.InProcessFitTestSystem;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.InProcessSlimClientBuilder;
import fitnesse.testsystems.slim.SlimClientBuilder;

public class TestSystemGroup {
  private Map<Descriptor, TestSystem> testSystems = new HashMap<Descriptor, TestSystem>();
  private FitNesseContext context;
  private TestSystemListener testSystemListener;
  private boolean fastTest = false;

  public TestSystemGroup(FitNesseContext context, TestSystemListener listener) {
    this.context = context;
    this.testSystemListener = listener;
  }

  public void kill() throws IOException {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.kill();
    }
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public TestSystem startTestSystem(Descriptor descriptor) throws IOException {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(descriptor)) {
      testSystem = makeTestSystem(descriptor);

      testSystems.put(descriptor, testSystem);
      testSystem.start();
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(Descriptor descriptor) throws IOException {
    if ("slim".equalsIgnoreCase(descriptor.getTestSystemType()))
      return fastTest ? createInProcessHtmlSlimTestSystem(descriptor) : createHtmlSlimTestSystem(descriptor);
    else
      return fastTest ? createInProcessFitTestSystem(descriptor) : createFitTestSystem(descriptor);
  }

  private HtmlSlimTestSystem createHtmlSlimTestSystem(Descriptor descriptor) throws IOException {
    SlimCommandRunningClient slimClient = new SlimClientBuilder(descriptor).build();
    HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystemName(), slimClient, testSystemListener);

    return testSystem;
  }

  private HtmlSlimTestSystem createInProcessHtmlSlimTestSystem(Descriptor descriptor) throws IOException {
    SlimCommandRunningClient slimClient = new InProcessSlimClientBuilder(descriptor).build();
    HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystemName(), slimClient, testSystemListener);

    return testSystem;
  }

  private FitTestSystem createFitTestSystem(Descriptor descriptor) throws IOException {
    FitTestSystem testSystem = new FitTestSystem(context, descriptor, testSystemListener);
    testSystem.build();

    return testSystem;
  }

  private FitTestSystem createInProcessFitTestSystem(Descriptor descriptor) throws IOException {
    FitTestSystem testSystem = new InProcessFitTestSystem(context, descriptor, testSystemListener);
    testSystem.build();

    return testSystem;
  }

}
