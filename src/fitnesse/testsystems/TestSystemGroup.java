// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.Collections;
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

  public TestSystemGroup(FitNesseContext context, TestSystemListener listener) {
    this.context = context;
    this.testSystemListener = listener;
  }

  public void kill() throws IOException {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.kill();
    }
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

  private static Map<String, TestSystemFactory> testSystemFactories;
  static {
    testSystemFactories = new HashMap<String, TestSystemFactory>(4);
    testSystemFactories.put("slim", new HtmlSlimTestSystemFactory());
    testSystemFactories.put("slim^inprocess", new InProcessHtmlSlimTestSystemFactory());
    testSystemFactories.put("fit", new FitTestSystemFactory());
    testSystemFactories.put("fit^inprocess", new InProcessFitTestSystemFactory());
  }

  private TestSystem makeTestSystem(Descriptor descriptor) throws IOException {
    TestSystemFactory factory = testSystemFactories.get(descriptor.getTestSystemType().toLowerCase());
    TestSystem testSystem = factory.create(descriptor, testSystemListener);
    return testSystem;
  }

  public static class HtmlSlimTestSystemFactory implements TestSystemFactory {

    public final TestSystem create(Descriptor descriptor, TestSystemListener testSystemListener) throws IOException {
      SlimCommandRunningClient slimClient = new SlimClientBuilder(descriptor).build();
      HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystemName(), slimClient, testSystemListener);

      return testSystem;
    }
  }

  public static class InProcessHtmlSlimTestSystemFactory implements TestSystemFactory {

    public TestSystem create(Descriptor descriptor, TestSystemListener testSystemListener) throws IOException {
      SlimCommandRunningClient slimClient = new InProcessSlimClientBuilder(descriptor).build();
      HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystemName(), slimClient, testSystemListener);

      return testSystem;
    }
  }

  public static class FitTestSystemFactory implements TestSystemFactory {

    public FitTestSystem create(Descriptor descriptor, TestSystemListener testSystemListener) throws IOException {
      int port = Integer.parseInt(descriptor.getVariable("FITNESSE_PORT"));
      FitTestSystem testSystem = new FitTestSystem(descriptor, port, testSystemListener);
      testSystem.build();

      return testSystem;
    }
  }

  public static class InProcessFitTestSystemFactory implements TestSystemFactory {

    public FitTestSystem create(Descriptor descriptor, TestSystemListener testSystemListener) throws IOException {
      int port = Integer.parseInt(descriptor.getVariable("FITNESSE_PORT"));
      FitTestSystem testSystem = new InProcessFitTestSystem(descriptor, port, testSystemListener);
      testSystem.build();

      return testSystem;
    }
  }
}
