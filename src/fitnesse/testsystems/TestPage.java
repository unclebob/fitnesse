package fitnesse.testsystems;

import java.util.List;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class TestPage {
  private WikiPage sourcePage;
  private PageData data;
  private List<WikiPage> scenarioLibraries;
  private WikiPage suiteSetUp;
  private WikiPage setUp;
  private WikiPage tearDown;
  private WikiPage suiteTearDown;

  public TestPage(WikiPage sourcePage) {
    this.sourcePage = sourcePage;
  }

  public TestPage(PageData data) {
    this.data = data;
    this.sourcePage = data.getWikiPage();
  }

  public WikiPage getSourcePage() {
    return sourcePage;
  }

  public PageData getData() {
    return data == null ? sourcePage.getData() : data;
  }

  public ReadOnlyPageData parsedData() {
    return sourcePage.readOnlyData();
  }

  /**
   * Obtain one big page containing all (suite-) setUp and -tearDown content needed to run a test.
   *
   * @return
   */
  public PageData getDecoratedData() {
    StringBuilder decoratedContent = new StringBuilder(1024);
    includeScenarioLibraries(decoratedContent);

    includePage(suiteSetUp, "-setup", decoratedContent);
    includePage(setUp, "-setup", decoratedContent);

    decoratedContent.append(parsedData().getContent());

    includePage(tearDown, "-setup", decoratedContent);
    includePage(suiteTearDown, "-setup", decoratedContent);

    return new PageData(sourcePage, decoratedContent.toString());
  }

  private void includeScenarioLibraries(StringBuilder decoratedContent) {
    if (scenarioLibraries != null) {
      decoratedContent.append("!*> Scenario Libraries\n");
      for (WikiPage scenarioLibrary : scenarioLibraries)
        includeScenarioLibrary(scenarioLibrary, decoratedContent);
      decoratedContent.append("*!\n");
    }
  }

  private void includeScenarioLibrary(WikiPage scenarioLibrary, StringBuilder newPageContent) {
    newPageContent.append("!include -c .");
    PageCrawler pageCrawler = getSourcePage().getPageCrawler();
    newPageContent.append(PathParser.render(pageCrawler.getFullPath(scenarioLibrary)));
    newPageContent.append("\n");
  }


  private void includePage(WikiPage wikiPage, String arg, StringBuilder newPageContent) {
    if (wikiPage == null)
      return;
    String pagePathName = getPathNameForPage(wikiPage);
    newPageContent
            .append("\n!include ")
            .append(arg)
            .append(" .")
            .append(pagePathName)
            .append("\n");
  }

  private String getPathNameForPage(WikiPage page) {
    PageCrawler pageCrawler = getSourcePage().getPageCrawler();
    WikiPagePath pagePath = pageCrawler.getFullPath(page);
    return PathParser.render(pagePath);
  }


  public String getName() {
    return sourcePage.getName();
  }

  public boolean isSlim() {
    return "slim".equalsIgnoreCase(parsedData().getVariable("TEST_SYSTEM"));
  }

  public boolean isTestPage() {
    return parsedData().hasAttribute("Test");
  }

  public void setScenarioLibraries(List<WikiPage> scenarioLibraries) {
    this.scenarioLibraries = scenarioLibraries;
  }

  public List<WikiPage> getScenarioLibraries() {
    return scenarioLibraries;
  }

  public void setSuiteSetUp(WikiPage suiteSetUp) {
    this.suiteSetUp = suiteSetUp;
  }

  public WikiPage getSuiteSetUp() {
    return suiteSetUp;
  }

  public void setSetUp(WikiPage setUp) {
    this.setUp = setUp;
  }

  public WikiPage getSetUp() {
    return setUp;
  }

  public void setTearDown(WikiPage tearDown) {
    this.tearDown = tearDown;
  }

  public WikiPage getTearDown() {
    return tearDown;
  }

  public void setSuiteTearDown(WikiPage suiteTearDown) {
    this.suiteTearDown = suiteTearDown;
  }

  public WikiPage getSuiteTearDown() {
    return suiteTearDown;
  }
}
