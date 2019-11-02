package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TestRunFactory {
  private static TestRunFactory ourInstance = new TestRunFactory();

  public static TestRunFactory getInstance() {
    return ourInstance;
  }

  private final List<Function<List<WikiPage>, Optional<TestRun>>> runProviders = new ArrayList<>();

  private TestRunFactory() {
  }

  public void resetProviders() {
    runProviders.clear();
  }

  public void addProvider(Function<List<WikiPage>, Optional<TestRun>> provider) {
    runProviders.add(0, provider);
  }

  public TestRun createRun(List<WikiPage> pages) {
    Optional<TestRun> run;
    for (Function<List<WikiPage>, Optional<TestRun>> provider : runProviders) {
      run = provider.apply(pages);
      if (run.isPresent()) {
        return run.get();
      }
    }
    return new PerTestSystemTestRun(pages);
  }
}
