package fitnesse.testrunner;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class TestPageWithSuiteSetUpAndTearDown extends WikiTestPage {

  private WikiPage suiteSetUp;
  private WikiPage suiteTearDown;

  public TestPageWithSuiteSetUpAndTearDown(WikiPage sourcePage) {
    super(sourcePage);
  }

  @Override
  protected String getDecoratedContent() {
    StringBuilder decoratedContent = new StringBuilder(1024);
    includeScenarioLibraries(decoratedContent);

    includePage(getSuiteSetUp(), "-setup", decoratedContent);
    includePage(getSetUp(), "-setup", decoratedContent);

    addPageContent(decoratedContent);

    includePage(getTearDown(), "-teardown", decoratedContent);
    includePage(getSuiteTearDown(), "-teardown", decoratedContent);

    return decoratedContent.toString();
  }

  public WikiPage getSuiteSetUp() {
    if (suiteSetUp == null && !isSuiteSetUpOrTearDownPage()) {
      suiteSetUp = findInheritedPage(PageData.SUITE_SETUP_NAME);
    }
    return suiteSetUp;
  }

  public WikiPage getSuiteTearDown() {
    if (suiteTearDown == null && !isSuiteSetUpOrTearDownPage()) {
      suiteTearDown = findInheritedPage(PageData.SUITE_TEARDOWN_NAME);
    }
    return suiteTearDown;
  }


}
