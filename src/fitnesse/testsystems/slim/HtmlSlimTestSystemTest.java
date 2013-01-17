package fitnesse.testsystems.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import fitnesse.testsystems.TestSystem;
import org.junit.Before;
import org.junit.Test;

import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Collapsible;
import fitnesse.wikitext.parser.Include;
import fitnesse.wikitext.parser.ParsedPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.test.ParserTestHelper;

public class HtmlSlimTestSystemTest {
  private WikiPage root;
  private PageCrawler crawler;
  private TestSystemListener dummyListener = new SlimTestSystemTest.DummyListener();

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    // Enforce the test runner here, to make sure we're talking to the right system
    HtmlSlimTestSystem.SlimDescriptor.clearSlimPortOffset();
  }

  @Test
  public void gettingPrecompiledScenarioWidgetsForChildLibraryPage() throws Exception {
    WikiPage suitePage = crawler.addPage(root, PathParser.parse("MySuite"), "my suite content");
    crawler.addPage(suitePage, PathParser.parse("ScenarioLibrary"), "child library");
    SlimTestSystem.Descriptor descriptor = HtmlSlimTestSystem.getDescriptor(suitePage, null, false);
    HtmlSlimTestSystem sys = new HtmlSlimTestSystem(suitePage, descriptor, dummyListener);

    ParsedPage scenarios = sys.getPreparsedScenarioLibrary();

    Symbol includeParent = getCollapsibleSymbol(scenarios.getSyntaxTree());
    assertNotNull(includeParent);
    assertEquals("Precompiled Libraries", ParserTestHelper.serializeContent(includeParent.childAt(0)));
    Symbol childLibraryInclude = getIncludeSymbol(includeParent.childAt(1));
    assertTrue(ParserTestHelper.serializeContent(childLibraryInclude).contains("child library"));
  }

  @Test
  public void gettingPrecompiledScenarioWidgetsForUncleLibraryPage() throws Exception {
    WikiPage suitePage = crawler.addPage(root, PathParser.parse("ParentPage.MySuite"), "my suite content");
    crawler.addPage(root, PathParser.parse("ScenarioLibrary"), "uncle library");
    TestSystem.Descriptor descriptor = HtmlSlimTestSystem.getDescriptor(suitePage, null, false);
    HtmlSlimTestSystem sys = new HtmlSlimTestSystem(suitePage, descriptor, dummyListener);

    ParsedPage scenarios = sys.getPreparsedScenarioLibrary();

    Symbol includeParent = getCollapsibleSymbol(scenarios.getSyntaxTree());
    assertNotNull(includeParent);
    assertEquals("Precompiled Libraries", ParserTestHelper.serializeContent(includeParent.childAt(0)));
    Symbol uncleLibraryInclude = getIncludeSymbol(includeParent.childAt(1));
    assertNotNull(uncleLibraryInclude);
    assertTrue(ParserTestHelper.serializeContent(uncleLibraryInclude).contains("uncle library"));
  }

  @Test
  public void precompiledScenarioWidgetsAreCreatedOnlyOnce() throws Exception {
    WikiPage suitePage = crawler.addPage(root, PathParser.parse("MySuite"), "my suite content");
    TestSystem.Descriptor descriptor = HtmlSlimTestSystem.getDescriptor(suitePage, null, false);
    HtmlSlimTestSystem sys = new HtmlSlimTestSystem(suitePage, descriptor, dummyListener);

    assertSame(sys.getPreparsedScenarioLibrary(), sys.getPreparsedScenarioLibrary());
  }

  private Symbol getIncludeSymbol(Symbol collapsibleSymbol) {
    for (Symbol symbol : collapsibleSymbol.getChildren())
      if (symbol.getType() instanceof Include)
        return symbol;
    return null;
  }

  private Symbol getCollapsibleSymbol(Symbol syntaxTree) throws Exception {
    for (Symbol symbol : syntaxTree.getChildren()) {
      if (symbol.getType() instanceof Collapsible)
        return symbol;
    }
    return null;
  }

}
