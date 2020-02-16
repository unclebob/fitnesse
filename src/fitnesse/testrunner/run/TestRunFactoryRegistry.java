package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.List;

public class TestRunFactoryRegistry {
  private final static TestRunFactoryRegistry INSTANCE = new TestRunFactoryRegistry();

  public static TestRunFactoryRegistry getInstance() {
    return INSTANCE;
  }

  public final static TestRunFactory DEFAULT =
    new TestRunFactory() {
      @Override
      public boolean canRun(List<WikiPage> pages) {
        return true;
      }

      @Override
      public TestRun createRun(List<WikiPage> pages) {
        return new PerTestSystemTestRun(pages);
      }
    };

  private final List<TestRunFactory> testRunFactories = new ArrayList<>();

  private TestRunFactoryRegistry() {
  }

  public void resetFactories() {
    testRunFactories.clear();
  }

  public void addFactory(TestRunFactory factory) {
    testRunFactories.add(0, factory);
  }

  public TestRun createRun(List<WikiPage> pages) {
    return getFactory(pages).createRun(pages);
  }

  public TestRunFactory getFactory(List<WikiPage> pages) {
    return testRunFactories.stream()
      .filter(f -> f.canRun(pages))
      .findFirst()
      .orElse(DEFAULT);
  }
}
