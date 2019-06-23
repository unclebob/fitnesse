package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.List;

public class TestRunFactory {
    private static TestRunFactory ourInstance = new TestRunFactory();

    public static TestRunFactory getInstance() {
        return ourInstance;
    }

    private TestRunFactory() {
    }

    public TestRun createRun(List<WikiPage> pages) {
      return new PerTestSystemTestRun(pages);
    }
}
