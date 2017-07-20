package fitnesse.testrunner;

import java.util.HashMap;
import java.util.Map;

import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.fit.CommandRunningFitClient;
import fitnesse.testsystems.fit.FitClientBuilder;
import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.fit.InProcessFitClientBuilder;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.InProcessSlimClientBuilder;
import fitnesse.testsystems.slim.SlimClient;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

public class MultipleTestSystemFactory implements TestSystemFactory, TestSystemFactoryRegistry {
  private final Map<String, TestSystemFactory> testSystemFactories = new HashMap<>(4);
  private final Map<String, TestSystemFactory> inProcessTestSystemFactories = new HashMap<>(4);

  public MultipleTestSystemFactory(SlimTableFactory slimTableFactory, CustomComparatorRegistry customComparatorRegistry) {
    registerTestSystemFactory("slim", new HtmlSlimTestSystemFactory(slimTableFactory, customComparatorRegistry));
    registerTestSystemFactory("fit", new FitTestSystemFactory());

    // This is basically the legacy: we want to be able to run slim and fit both in process and out of process.
    registerInProcessTestSystemFactory("slim", new InProcessHtmlSlimTestSystemFactory(slimTableFactory, customComparatorRegistry));
    registerInProcessTestSystemFactory("fit", new InProcessFitTestSystemFactory());
  }

  @Override
  public void registerTestSystemFactory(String name, TestSystemFactory testSystemFactory) {
    testSystemFactories.put(name, testSystemFactory);
  }

  public void registerInProcessTestSystemFactory(String name, TestSystemFactory testSystemFactory) {
    inProcessTestSystemFactories.put(name, testSystemFactory);
  }

  @Override
  public TestSystem create(Descriptor descriptor) {
    TestSystemFactory factory = null;
    if (descriptor.runInProcess()) {
      factory = inProcessTestSystemFactories.get(descriptor.getTestSystemType().toLowerCase());
    }
    if (factory == null) {
      factory = testSystemFactories.get(descriptor.getTestSystemType().toLowerCase());
    }
    if (factory == null) {
      throw new RuntimeException(String.format("Unknown test system: '%s'", descriptor.getTestSystemType()));
    }
    return factory.create(descriptor);
  }

  static class HtmlSlimTestSystemFactory implements TestSystemFactory {
    private final SlimTableFactory slimTableFactory;
    private final CustomComparatorRegistry customComparatorRegistry;

    public HtmlSlimTestSystemFactory(SlimTableFactory slimTableFactory,
                                     CustomComparatorRegistry customComparatorRegistry) {
      this.slimTableFactory = slimTableFactory;
      this.customComparatorRegistry = customComparatorRegistry;
    }

    @Override
    public final TestSystem create(Descriptor descriptor) {
      SlimClientBuilder clientBuilder = new SlimClientBuilder(descriptor);
      SlimCommandRunningClient slimClient = clientBuilder.build();
      HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(clientBuilder.getTestSystemName(), slimClient,
              slimTableFactory.copy(), customComparatorRegistry);

      return testSystem;
    }
  }

  static class InProcessHtmlSlimTestSystemFactory implements TestSystemFactory {
    private final SlimTableFactory slimTableFactory;
    private final CustomComparatorRegistry customComparatorRegistry;

    public InProcessHtmlSlimTestSystemFactory(SlimTableFactory slimTableFactory,
                                              CustomComparatorRegistry customComparatorRegistry) {
      this.slimTableFactory = slimTableFactory;
      this.customComparatorRegistry = customComparatorRegistry;
    }

    @Override
    public TestSystem create(Descriptor descriptor) {
      InProcessSlimClientBuilder clientBuilder = new InProcessSlimClientBuilder(descriptor);
      SlimClient slimClient = clientBuilder.build();
      HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(clientBuilder.getTestSystemName(), slimClient,
              slimTableFactory.copy(), customComparatorRegistry);

      return testSystem;
    }
  }

  static class FitTestSystemFactory implements TestSystemFactory {

    @Override
    public FitTestSystem create(Descriptor descriptor) {
      FitClientBuilder clientBuilder = new FitClientBuilder(descriptor);
      CommandRunningFitClient fitClient = clientBuilder.build();

      return new FitTestSystem(clientBuilder.getTestSystemName(), fitClient);
    }
  }

  static class InProcessFitTestSystemFactory implements TestSystemFactory {

    @Override
    public FitTestSystem create(Descriptor descriptor) {
      InProcessFitClientBuilder clientBuilder = new InProcessFitClientBuilder(descriptor);
      CommandRunningFitClient fitClient = clientBuilder.build();

      return new FitTestSystem(clientBuilder.getTestSystemName(), fitClient);
    }
  }
}
