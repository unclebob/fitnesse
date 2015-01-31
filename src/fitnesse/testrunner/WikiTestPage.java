package fitnesse.testrunner;

import java.util.LinkedList;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.WikiSourcePage;

public class WikiTestPage implements TestPage {
  public static final String TEAR_DOWN = "TearDown";
  public static final String SET_UP = "SetUp";

  private final WikiPage sourcePage;
  private final VariableSource variableSource;
  private List<WikiPage> scenarioLibraries;
  private WikiPage setUp;
  private WikiPage tearDown;

  public WikiTestPage(WikiPage sourcePage, VariableSource variableSource) {
    this.sourcePage = sourcePage;
    this.variableSource = variableSource;
  }

  public static boolean isTestPage(WikiPage page) {
    return isTestPage(page.getData());
  }
  public static boolean isTestPage(ReadOnlyPageData pageData) {
    return pageData.hasAttribute("Test");
  }

  public PageData getData() {
    return sourcePage.getData();
  }

  @Override
  public String getHtml() {
    String content = getDecoratedContent();
    ParsingPage parsingPage = new ParsingPage(new WikiSourcePage(sourcePage), variableSource);
    Symbol syntaxTree = Parser.make(parsingPage, content).parse();
    return new HtmlTranslator(parsingPage.getPage(), parsingPage, 
            sourcePage.getVariable(WikiPageIdentity.TEST_SYSTEM)).translateTree(syntaxTree);
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


  protected String getPathSeparator() {
    String separator = sourcePage.getVariable(PageData.PATH_SEPARATOR);
    if (separator == null)
      separator = System.getProperty("path.separator");
    return separator;
  }

  public WikiPage getSourcePage() {
    return sourcePage;
  }

  protected String getDecoratedContent() {
    StringBuilder decoratedContent = new StringBuilder(1024);
    includeScenarioLibraries(decoratedContent);

    decorate(getSetUp(), decoratedContent);

    addPageContent(decoratedContent);

    decorate(getTearDown(), decoratedContent);
    return decoratedContent.toString();
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
      decoratedContent.append(wikiPage.getData().getContent());
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
    // Should consider all of the decorated content to resolve those variables.
    boolean isSlim = "slim".equalsIgnoreCase(sourcePage.getVariable(WikiPageIdentity.TEST_SYSTEM));
    String includeScenarioLibraries = sourcePage.getVariable("INCLUDE_SCENARIO_LIBRARIES");
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
