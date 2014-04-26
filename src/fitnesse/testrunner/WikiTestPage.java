package fitnesse.testrunner;

import java.util.LinkedList;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class WikiTestPage implements TestPage {
  public static final String TEAR_DOWN = "TearDown";
  public static final String SET_UP = "SetUp";

  private WikiPage sourcePage;
  private PageData data;
  private List<WikiPage> scenarioLibraries;
  private WikiPage setUp;
  private WikiPage tearDown;

  public WikiTestPage(WikiPage sourcePage) {
    this.sourcePage = sourcePage;
  }

  public WikiTestPage(PageData data) {
    this.data = data;
    this.sourcePage = data.getWikiPage();
  }

  public static boolean isTestPage(ReadOnlyPageData pageData) {
    return pageData.hasAttribute("Test");
  }

  public WikiPage getSourcePage() {
    return sourcePage;
  }

  public PageData getData() {
    return data == null ? sourcePage.getData() : data;
  }

  /**
   * Obtain one big page containing all (suite-) setUp and -tearDown content needed to run a test.
   *
   * @return
   */
  @Override
  public ReadOnlyPageData getDecoratedData() {
    StringBuilder decoratedContent = new StringBuilder(1024);
    includeScenarioLibraries(decoratedContent);

    decorate(getSetUp(), decoratedContent);

    addPageContent(decoratedContent);

    decorate(getTearDown(), decoratedContent);

    return new PageData(getSourcePage().getData(), decoratedContent.toString());
  }

  protected void addPageContent(StringBuilder decoratedContent) {
    String content = getData().getContent();
    decoratedContent
            .append("\n")
            .append(content)
            .append(content.endsWith("\n") ? "" : "\n");
  }

  protected void decorate(WikiPage wikiPage, StringBuilder decoratedContent) {
    if (wikiPage == getSetUp()) {
      includePage(wikiPage, "-setup", decoratedContent);
    } else if (wikiPage == getTearDown()) {
      includePage(wikiPage, "-teardown", decoratedContent);
    } else if (getScenarioLibraries().contains(wikiPage)) {
      includeScenarioLibrary(wikiPage, decoratedContent);
    } else {
      decoratedContent.append(wikiPage.readOnlyData().getContent());
    }
  }

  protected void includeScenarioLibraries(StringBuilder decoratedContent) {
    final List<WikiPage> libraries = getScenarioLibraries();
    if (!libraries.isEmpty()) {
      if (libraries.size() > 1) {
        decoratedContent.append("!*> Scenario Libraries\n");
      }

      for (WikiPage scenarioLibrary : libraries) {
        includeScenarioLibrary(scenarioLibrary, decoratedContent);
      }

      if (libraries.size() > 1) {
        decoratedContent.append("*!\n");
      }
    }
  }

  protected void includeScenarioLibrary(WikiPage scenarioLibrary, StringBuilder newPageContent) {
    newPageContent.append("!include -c .");
    newPageContent.append(getPathNameForPage(scenarioLibrary));
    newPageContent.append("\n");
  }

  protected void includePage(WikiPage wikiPage, String arg, StringBuilder newPageContent) {
    if (wikiPage == null)
      return;
    String pagePathName = getPathNameForPage(wikiPage);
    newPageContent
            .append("!include ")
            .append(arg)
            .append(" .")
            .append(pagePathName)
            .append("\n");
  }

  private String getPathNameForPage(WikiPage page) {
    WikiPagePath pagePath = page.getPageCrawler().getFullPath();
    return PathParser.render(pagePath);
  }

  public String getPath() {
    return getPathNameForPage(sourcePage);
  }

  public String getName() {
    return sourcePage.getName();
  }

  public boolean shouldIncludeScenarioLibraries() {
    boolean isSlim = "slim".equalsIgnoreCase(getData().getVariable(WikiPageIdentity.TEST_SYSTEM));
    String includeScenarioLibraries = getData().getVariable("INCLUDE_SCENARIO_LIBRARIES");
    boolean includeScenarios = "true".equalsIgnoreCase(includeScenarioLibraries);
    boolean notIncludeScenarios = "false".equalsIgnoreCase(includeScenarioLibraries);

    return includeScenarios || (!notIncludeScenarios && isSlim);
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
    return sourcePage.getPageCrawler().getClosestInheritedPage(pageName);
  }

  private List<WikiPage> findScenarioLibraries() {
    final LinkedList<WikiPage> uncles = new LinkedList<WikiPage>();
    if (shouldIncludeScenarioLibraries()) {
      sourcePage.getPageCrawler().traverseUncles("ScenarioLibrary", new TraversalListener<WikiPage>() {
        @Override
        public void process(WikiPage page) {
          uncles.addFirst(page);
        }
      });
    }
    return uncles;
  }

}
