package fitnesse.testsystems;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class TestPageWithSuiteSetUpAndTearDown extends TestPage {

  private WikiPage suiteSetUp;
  private WikiPage suiteTearDown;

  public TestPageWithSuiteSetUpAndTearDown(WikiPage sourcePage) {
    super(sourcePage);
  }

  public PageData getDecoratedData() {
    StringBuilder decoratedContent = new StringBuilder(1024);
    includeScenarioLibraries(decoratedContent);

    includePage(getSuiteSetUp(), "-setup", decoratedContent);
    includePage(getSetUp(), "-setup", decoratedContent);

    decoratedContent.append(parsedData().getContent());

    includePage(getTearDown(), "-teardown", decoratedContent);
    includePage(getSuiteTearDown(), "-teardown", decoratedContent);

    return new PageData(getSourcePage(), decoratedContent.toString());
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
