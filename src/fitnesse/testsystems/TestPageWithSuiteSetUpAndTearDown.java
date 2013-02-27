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

    decorate(getSuiteSetUp(), decoratedContent);

    decorate(getSetUp(), decoratedContent);

    decoratedContent.append(parsedData().getContent());

    decorate(getTearDown(), decoratedContent);

    decorate(getSuiteTearDown(), decoratedContent);

    return new PageData(getSourcePage(), decoratedContent.toString());
  }

  protected void decorate(WikiPage wikiPage, StringBuilder decoratedContent) {
    if (wikiPage == getSuiteSetUp() || wikiPage == getSuiteTearDown()) {
      includePage(wikiPage, true, decoratedContent);
    } else {
      super.decorate(wikiPage, decoratedContent);
    }
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
