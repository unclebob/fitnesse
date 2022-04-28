package fitnesse.testrunner;

import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.*;
import fitnesse.wikitext.MarkUpSystem;
import fitnesse.wikitext.parser.Include;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

// TODO: need 2 implementations, one for wiki text pages (Fit, Slim) and one for non-wiki text pages. See PagesByTestSystem
public class WikiTestPage implements TestPage {
  public static final String TEAR_DOWN = "TearDown";
  public static final String SET_UP = "SetUp";
  public static final String SCENARIO_LIBRARY = "ScenarioLibrary";

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
      return MarkUpSystem.make().parse(BaseWikitextPage.makeParsingPage((BaseWikitextPage) sourcePage), content).translateToHtml();
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
    return PathParser.render(sourcePage.getFullPath());
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

    includeSetUps(decoratedContent);

    addPageContent(decoratedContent);

    includeTearDowns(decoratedContent);

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
    includePages("Scenario Libraries", libraries, this::includeScenarioLibrary, decoratedContent);
  }

  protected void includeSetUps(StringBuilder decoratedContent) {
    includeSetUp(getSetUp(), decoratedContent);
  }

  protected void includeTearDowns(StringBuilder decoratedContent) {
    includeTearDown(getTearDown(), decoratedContent);
  }

  protected void includeScenarioLibrary(WikiPage scenarioLibrary, StringBuilder newPageContent) {
    includePage(scenarioLibrary, Include.COLLAPSE_ARG, newPageContent);
  }

  protected void includeSetUp(WikiPage suiteSetUp, StringBuilder newPageContent) {
    includePage(suiteSetUp, Include.SETUP_ARG, newPageContent);
  }

  protected void includeTearDown(WikiPage suiteTearDown, StringBuilder newPageContent) {
    includePage(suiteTearDown, Include.TEARDOWN_ARG, newPageContent);
  }

  protected void includePages(String name, List<WikiPage> pages, BiConsumer<WikiPage, StringBuilder> includePage,
                              StringBuilder decoratedContent) {
    if (pages != null && !pages.isEmpty()) {
      boolean multiplePages = pages.size() > 1;
      if (multiplePages) {
        decoratedContent.append("!*> ");
        decoratedContent.append(name);
        decoratedContent.append("\n");
      }

      for (WikiPage page : pages) {
        includePage.accept(page, decoratedContent);
      }

      if (multiplePages) {
        decoratedContent.append("*!\n");
      }
    }
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
    WikiPagePath pagePath = page.getFullPath();
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

  public boolean shouldIncludeTagLibraries() {
    String includeVar = sourcePage.getVariable("INCLUDE_TAG_LIBRARIES");
    return includeVar != null && "true".equalsIgnoreCase(includeVar);
  }

  public List<WikiPage> getScenarioLibraries() {
    if (scenarioLibraries == null) {
      scenarioLibraries = findScenarioLibraries();
      scenarioLibraries.addAll(findTagLibraries());
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
    return sourcePage.isSuiteSetupOrTearDown();
  }

  protected WikiPage findInheritedPage(String pageName) {
    return sourcePage.getPageCrawler().getClosestInheritedPage(pageName);
  }

  private List<WikiPage> findScenarioLibraries() {
    List<WikiPage> uncles;
    if (shouldIncludeScenarioLibraries()) {
      uncles = findUncles(SCENARIO_LIBRARY);
    } else {
      uncles = new LinkedList<>();
    }
    return uncles;
  }

  private List<WikiPage> findTagLibraries() {
    List<WikiPage> pages = new LinkedList<>();
    WikiPageProperty suitesProperty = sourcePage.getData().getProperties().getProperty(WikiPageProperty.SUITES);
    if (shouldIncludeTagLibraries() && suitesProperty != null){
      String[] tags1 = Arrays.stream(suitesProperty.getValue().split(",")).map(e -> e.trim()).filter(e -> checkIfTagIsNotReserved(e)).toArray(String[]::new);
      for(int i=0;i<tags1.length;i++){
        pages.addAll(findUncles(tags1[i].trim()));
      }
    }
    return pages;
  }

  private boolean checkIfTagIsNotReserved(String s){
    return !(s.trim().equals(SET_UP) || s.trim().equals(TEAR_DOWN) || s.trim().equals(SCENARIO_LIBRARY));
  }

  protected List<WikiPage> findUncles(String uncleName) {
    LinkedList<WikiPage> uncles = new LinkedList<>();
    sourcePage.getPageCrawler().traverseUncles(uncleName, uncles::addFirst);
    return uncles;
  }
}
