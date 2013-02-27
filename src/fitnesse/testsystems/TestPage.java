package fitnesse.testsystems;

import java.util.Collections;
import java.util.List;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.GracefulNamer;

public class TestPage {
  public static final String TEAR_DOWN = "TearDown";
  public static final String SET_UP = "SetUp";

  private WikiPage sourcePage;
  private PageData data;
  private List<WikiPage> scenarioLibraries;
  private WikiPage setUp;
  private WikiPage tearDown;

  public TestPage(WikiPage sourcePage) {
    this.sourcePage = sourcePage;
  }

  public TestPage(PageData data) {
    this.data = data;
    this.sourcePage = data.getWikiPage();
  }

  public static boolean isTestPage(PageData pageData) {
    return pageData.hasAttribute("Test");
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

    decorate(getSetUp(), decoratedContent);

    decoratedContent.append(parsedData().getContent());

    decorate(getTearDown(), decoratedContent);

    return new PageData(sourcePage, decoratedContent.toString());
  }

  public PageData decorate(WikiPage wikiPage) {
    StringBuilder decoratedContent = new StringBuilder(1024);
    decorate(wikiPage, decoratedContent);
    return new PageData(sourcePage, decoratedContent.toString());
  }

  protected void decorate(WikiPage wikiPage, StringBuilder decoratedContent) {
    if (wikiPage == getSetUp()) {
      includePage(wikiPage, collapse("SETUP"), decoratedContent);
    } else if (wikiPage == getTearDown()) {
      includePage(wikiPage, collapse("TEARDOWN"), decoratedContent);
    } else if (getScenarioLibraries().contains(wikiPage)) {
      includePage(wikiPage, true, decoratedContent);
    } else {
      decoratedContent.append(wikiPage.readOnlyData().getContent());
    }
  }

  protected boolean collapse(String page) {
    String collapse = sourcePage.getData().getVariable("COLLAPSE_" + page);
    return collapse == null || "true".equals(collapse);
  }

  protected void includeScenarioLibraries(StringBuilder decoratedContent) {
    if (!getScenarioLibraries().isEmpty()) {
      decoratedContent.append("!*> Scenario Libraries\n");
      for (WikiPage scenarioLibrary : getScenarioLibraries()) {
        includePage(scenarioLibrary, true, decoratedContent);
      }
      decoratedContent.append("*!\n");
    }
  }

  protected void includePage(WikiPage wikiPage, boolean collapse, StringBuilder newPageContent) {
    if (wikiPage == null)
      return;
    String pagePathName = getPathNameForPage(wikiPage);
    String content = wikiPage.readOnlyData().getContent();
    newPageContent
            .append("\n!*")
            .append(collapse ? ">" : "")
            .append(" !-")
            .append(GracefulNamer.regrace(wikiPage.getName()))
            .append("-! page: .")
            .append(pagePathName)
            .append("\n")
            .append(content)
            .append(content.endsWith("\n") ? "" : "\n")
            .append("*!\n");
  }

  private String getPathNameForPage(WikiPage page) {
    PageCrawler pageCrawler = getSourcePage().getPageCrawler();
    WikiPagePath pagePath = pageCrawler.getFullPath(page);
    return PathParser.render(pagePath);
  }

  public String getPath() {
    return getPathNameForPage(sourcePage);
  }

  public String getName() {
    return sourcePage.getName();
  }

  public boolean isSlim() {
    return "slim".equalsIgnoreCase(parsedData().getVariable("TEST_SYSTEM"));
  }

  public boolean isTestPage() {
    return isTestPage(getData());
  }

  public List<WikiPage> getScenarioLibraries() {
    if (scenarioLibraries == null) {
      scenarioLibraries = findScenarioLibraries();
    }
    return scenarioLibraries;
  }

  public WikiPage getSetUp() {
    if (setUp == null && !isSuiteSetUpOrTearDownPage()) {
      setUp = findInheritedPage(SET_UP);
    }
    return setUp;
  }

  public WikiPage getTearDown() {
    if (tearDown == null && !isSuiteSetUpOrTearDownPage()) {
      tearDown = findInheritedPage(TEAR_DOWN);
    }
    return tearDown;
  }

  protected boolean isSuiteSetUpOrTearDownPage() {
    return PageData.SUITE_SETUP_NAME.equals(getName()) || PageData.SUITE_TEARDOWN_NAME.equals(getName());
  }

  protected WikiPage findInheritedPage(String pageName) {
    return PageCrawlerImpl.getClosestInheritedPage(pageName, sourcePage);
  }

  private List<WikiPage> findScenarioLibraries() {
    List<WikiPage> uncles;
    if (isSlim()) {
      uncles = PageCrawlerImpl.getAllUncles("ScenarioLibrary", sourcePage);
      Collections.reverse(uncles);
    } else {
      uncles = Collections.emptyList();
    }
    return uncles;
  }

}
