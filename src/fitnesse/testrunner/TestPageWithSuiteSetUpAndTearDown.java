package fitnesse.testrunner;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class TestPageWithSuiteSetUpAndTearDown extends WikiTestPage {

  private WikiPage suiteSetUp;
  private WikiPage suiteTearDown;

  public TestPageWithSuiteSetUpAndTearDown(WikiPage sourcePage) {
    super(sourcePage);
  }

  @Override
  protected void includeSetUps(StringBuilder decoratedContent) {
    includeSetUp(getSuiteSetUp(), decoratedContent);
    super.includeSetUps(decoratedContent);
  }

  @Override
  protected void includeTearDowns(StringBuilder decoratedContent) {
    super.includeTearDowns(decoratedContent);
    includeTearDown(getSuiteTearDown(), decoratedContent);
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
