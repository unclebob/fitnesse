package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.List;

public interface TestRunFactory {
  boolean canRun(List<WikiPage> pages);

  TestRun createRun(List<WikiPage> pages);

  PagePositions findPagePositions(List<WikiPage> pages);
}
