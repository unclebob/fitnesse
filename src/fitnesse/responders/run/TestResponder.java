package fitnesse.responders.run;

import java.util.List;

import fitnesse.wiki.WikiPage;

import static fitnesse.testrunner.WikiTestPage.isTestPage;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class TestResponder extends SuiteResponder {

  @Override
  protected List<WikiPage> getPagesToRun() {
    if (isTestPage(page)) {
      return asList(page);
    } else {
      return emptyList();
    }
  }
}
