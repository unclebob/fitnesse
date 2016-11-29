package fitnesse.testrunner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.BaseWikitextPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.WikiSourcePage;

// TODO: need 2 implementations, one for wiki text pages (Fit, Slim) and one for non-wiki text pages. See PagesByTestSystem
public class WikiTestPage implements TestPage {
  public static final String TEAR_DOWN = "TearDown";
  public static final String SET_UP = "SetUp";

  private final WikiPage sourcePage;
  private List<WikiPage> scenarioLibraries;
  private WikiPage setUp;
  private WikiPage tearDown;

  public WikiTestPage(WikiPage sourcePage) {
    this.sourcePage = sourcePage;
  }

  public PageData getData() {
    return sourcePage.getData();
  }

  @Override
  public String getHtml() {

    // -AJM- Okay, this is not as clean as I'd like it to be, but for now it does the trick
    if (containsWikitext()) {
      String content = getDecoratedContent();
      ParsingPage parsingPage = BaseWikitextPage.makeParsingPage((BaseWikitextPage) sourcePage);

      Symbol syntaxTree = Parser.make(parsingPage, content).parse();

      return new HtmlTranslator(new WikiSourcePage(sourcePage), parsingPage).translateTree(syntaxTree);
    } else {
      return sourcePage.getHtml();
    }
  }

  private boolean containsWikitext() {
    return SymbolicPage.containsWikitext(sourcePage);
  }

  @Override
  public String getVariable(String variable) {
    return sourcePage.getVariable(variable);
  }

  @Override
  public String getFullPath() {
    return PathParser.render(sourcePage.getPageCrawler().getFullPath());
  }

  @Override
  public ClassPath getClassPath() {
    return new ClassPath(new ClassPathBuilder().getClassPath(sourcePage), getPathSeparator());
  }

  @Override
  public String getContent() {
    if (containsWikitext()) {
      return getDecoratedContent();
    } else {
      return sourcePage.getData().getContent();
    }
  }

  protected String getPathSeparator() {
    String separator = sourcePage.getVariable(PageData.PATH_SEPARATOR);
    if (separator == null)
      separator = File.pathSeparator;
    return separator;
  }


  public WikiPage getSourcePage() {
    return sourcePage;
  }

  protected String getDecoratedContent() {
    StringBuilder decoratedContent = new StringBuilder(1024);
    includeScenarioLibraries(decoratedContent);

    includePage(getSetUp(), "-setup", decoratedContent);

    addPageContent(decoratedContent);

    includePage(getTearDown(), "-teardown", decoratedContent);

    return decoratedContent.toString();
  }

  protected void addPageContent(StringBuilder decoratedContent) {
    String content = getData().getContent();
    decoratedContent
            .append("\n")
            .append(content)
            .append(content.endsWith("\n") ? "" : "\n");
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

  @Override
  public String getName() {
    return sourcePage.getName();
  }

  public boolean shouldIncludeScenarioLibraries() {
    // Should consider all of the decorated content to resolve those variables.
    String testSystem = sourcePage.getVariable(WikiPageIdentity.TEST_SYSTEM);
    boolean isSlim = "slim".equalsIgnoreCase(testSystem)
                      || "slimCoverage".equalsIgnoreCase(testSystem);
    String includeScenarioLibraries = sourcePage.getVariable("INCLUDE_SCENARIO_LIBRARIES");
    boolean includeScenarios = "true".equalsIgnoreCase(includeScenarioLibraries);
    boolean notIncludeScenarios = "false".equalsIgnoreCase(includeScenarioLibraries);

    return includeScenarios || (!notIncludeScenarios && isSlim);
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
    return isSuiteSetupOrTearDown(sourcePage);
  }

  protected WikiPage findInheritedPage(String pageName) {
    return sourcePage.getPageCrawler().getClosestInheritedPage(pageName);
  }

  private List<WikiPage> findScenarioLibraries() {
    final LinkedList<WikiPage> uncles = new LinkedList<>();
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

  public static boolean isSuiteSetupOrTearDown(WikiPage wikiPage) {
    String name = wikiPage.getName();
    return (PageData.SUITE_SETUP_NAME.equals(name) || PageData.SUITE_TEARDOWN_NAME.equals(name));
  }
}
