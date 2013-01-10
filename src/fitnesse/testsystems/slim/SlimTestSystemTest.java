// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.ServerSocket;
import java.net.SocketException;

import fitnesse.testsystems.TestSummary;
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
import org.junit.Before;
import org.junit.Test;

public class SlimTestSystemTest {
  private WikiPage root;
  private PageCrawler crawler;
  public String testResults;
  private TestSystemListener dummyListener = new DummyListener();

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    // Enforce the test runner here, to make sure we're talking to the right system
    SlimTestSystem.clearSlimPortOffset();
  }

  @Test
  public void portRotates() throws Exception {
    SlimTestSystem sys = new HtmlSlimTestSystem(root, dummyListener);
    for (int i = 1; i < 15; i++)
      assertEquals(8085 + (i % 10), sys.getNextSlimSocket());
  }

  @Test
  public void portStartsAtSlimPortVariable() throws Exception {
    WikiPage pageWithSlimPortDefined = crawler.addPage(root, PathParser.parse("PageWithSlimPortDefined"), "!define SLIM_PORT {9000}\n");
    SlimTestSystem sys = new HtmlSlimTestSystem(pageWithSlimPortDefined, dummyListener);
    for (int i = 1; i < 15; i++)
      assertEquals(9000 + (i % 10), sys.getNextSlimSocket());
  }

  @Test
  public void badSlimPortVariableDefaults() throws Exception {
    WikiPage pageWithBadSlimPortDefined = crawler.addPage(root, PathParser.parse("PageWithBadSlimPortDefined"), "!define SLIM_PORT {BOB}\n");
    SlimTestSystem sys = new HtmlSlimTestSystem(pageWithBadSlimPortDefined, dummyListener);
    for (int i = 1; i < 15; i++)
      assertEquals(8085 + (i % 10), sys.getNextSlimSocket());
  }

  @Test
  public void slimHostDefaultsTolocalhost() throws Exception {
    WikiPage pageWithoutSlimHostVariable = crawler.addPage(root, PathParser.parse("PageWithoutSlimHostVariable"), "some gunk\n");
    SlimTestSystem sys = new HtmlSlimTestSystem(pageWithoutSlimHostVariable, dummyListener);
    assertEquals("localhost", sys.determineSlimHost());
  }

  @Test
  public void slimHostVariableSetsTheHost() throws Exception {
    WikiPage pageWithSlimHostVariable = crawler.addPage(root, PathParser.parse("PageWithSlimHostVariable"), "!define SLIM_HOST {somehost}\n");
    SlimTestSystem sys = new HtmlSlimTestSystem(pageWithSlimHostVariable, dummyListener);
    assertEquals("somehost", sys.determineSlimHost());
  }

  @Test
  public void translateExceptionMessage() throws Exception {
    assertTranslatedException("Could not find constructor for SomeClass", "NO_CONSTRUCTOR SomeClass");
    assertTranslatedException("Could not invoke constructor for SomeClass", "COULD_NOT_INVOKE_CONSTRUCTOR SomeClass");
    assertTranslatedException("No converter for SomeClass", "NO_CONVERTER_FOR_ARGUMENT_NUMBER SomeClass");
    assertTranslatedException("Method someMethod not found in SomeClass", "NO_METHOD_IN_CLASS someMethod SomeClass");
    assertTranslatedException("The instance someInstance does not exist", "NO_INSTANCE someInstance");
    assertTranslatedException("Could not find class SomeClass", "NO_CLASS SomeClass");
    assertTranslatedException("The instruction [a, b, c] is malformed", "MALFORMED_INSTRUCTION [a, b, c]");
  }

  private void assertTranslatedException(String expected, String message) {
    assertEquals(expected, SlimTestSystem.translateExceptionMessage(message));
  }


  @Test(expected = SocketException.class)
  public void createSlimServiceFailsFastWhenSlimPortIsNotAvailable() throws Exception {
    final int slimServerPort = 10258;
    ServerSocket slimSocket = new ServerSocket(slimServerPort);
    try {
      SlimTestSystem sys = new HtmlSlimTestSystem(root, dummyListener);
      String slimArguments = String.format("%s %d", "", slimServerPort);
      sys.createSlimService(slimArguments);
    } finally {
      slimSocket.close();
    }
  }

  @Test
  public void gettingPrecompiledScenarioWidgetsForChildLibraryPage() throws Exception {
    WikiPage suitePage = crawler.addPage(root, PathParser.parse("MySuite"), "my suite content");
    crawler.addPage(suitePage, PathParser.parse("ScenarioLibrary"), "child library");
    SlimTestSystem sys = new HtmlSlimTestSystem(suitePage, dummyListener);

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
    SlimTestSystem sys = new HtmlSlimTestSystem(suitePage, dummyListener);

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
    SlimTestSystem sys = new HtmlSlimTestSystem(suitePage, dummyListener);

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

  private static class DummyListener implements TestSystemListener {
    public void acceptOutputFirst(String output) {
    }

    public void testComplete(TestSummary testSummary) {
    }

    public void exceptionOccurred(Throwable e) {
    }
  }
}
