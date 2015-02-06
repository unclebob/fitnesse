// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.EditResponder;
import fitnesse.testrunner.MultipleTestSystemFactory;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.HtmlTable;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.testutil.SimpleAuthenticator;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wikitext.parser.ParseSpecification;
import fitnesse.wikitext.parser.ScanString;
import fitnesse.wikitext.parser.SymbolMatch;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolStream;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Today;
import fitnesse.wikitext.parser.VariableSource;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PluginsLoaderTest {
  private Properties testProperties;
  private PluginsLoader loader;
  private ResponderFactory responderFactory;
  private SymbolProvider testProvider;
  private WikiPageFactoryRegistry testWikiPageFactoryRegistry;
  private SlimTableFactory testSlimTableFactory;
  private CustomComparatorRegistry testCustomComparatorsRegistry;
  private MultipleTestSystemFactory testTestSystemFactory;

  @Before
  public void setUp() throws Exception {
    testProperties = new Properties();
    responderFactory = new ResponderFactory(".");
    testProvider = new SymbolProvider(new SymbolType[] {});
    testWikiPageFactoryRegistry = new FileSystemPageFactory();
    testSlimTableFactory = new SlimTableFactory();
    testCustomComparatorsRegistry = new CustomComparatorRegistry();
    testTestSystemFactory = new MultipleTestSystemFactory(testSlimTableFactory, testCustomComparatorsRegistry);
    loader = new PluginsLoader(new ComponentFactory(testProperties));

    assertSymbolTypeMatch("!today", false);
  }

  @Test
  public void testAddPlugins() throws Exception {
    testProperties.setProperty(ConfigurationParameter.PLUGINS.getKey(), DummyPlugin.class.getName());

    loader.loadPlugins(responderFactory, testProvider, testWikiPageFactoryRegistry,
            testTestSystemFactory, testSlimTableFactory, testCustomComparatorsRegistry);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
    assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
    assertSymbolTypeMatch("!today", true);
  }

  @Test
  public void shouldHandleInstanceMethods() throws Exception {
    testProperties.setProperty(ConfigurationParameter.PLUGINS.getKey(), InstantiableDummyPlugin.class.getName());
    testProperties.setProperty("responderName", "instanceTest");

    loader.loadPlugins(responderFactory, testProvider, testWikiPageFactoryRegistry,
            testTestSystemFactory, testSlimTableFactory, testCustomComparatorsRegistry);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("instanceTest"));
  }

  private void assertSymbolTypeMatch(String input, boolean expected) {
    SymbolMatch match = new ParseSpecification().provider(testProvider).findMatch(new ScanString(input, 0), 0, new SymbolStream());
    assertEquals(match.isMatch(), expected);
  }

  @Test
  public void testAddResponderPlugins() throws Exception {
    String respondersValue = "custom1:" + WikiPageResponder.class.getName() + ",custom2:" + EditResponder.class.getName();
    testProperties.setProperty(ConfigurationParameter.RESPONDERS.getKey(), respondersValue);

    loader.loadResponders(responderFactory);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
    assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
  }

  @Test
  public void testWikiWidgetPlugins() throws Exception {
    String symbolValues = Today.class.getName();
    testProperties.setProperty(ConfigurationParameter.SYMBOL_TYPES.getKey(), symbolValues);

    loader.loadSymbolTypes(testProvider);

    assertSymbolTypeMatch("!today", true);
  }

  @Test
  public void testAuthenticatorDefaultCreation() throws Exception {
    Authenticator authenticator = loader.getAuthenticator(new PromiscuousAuthenticator());
    assertNotNull(authenticator);
    assertEquals(PromiscuousAuthenticator.class, authenticator.getClass());
  }

  @Test
  public void testAuthenticatorCustomCreation() throws Exception {
    testProperties.setProperty(ConfigurationParameter.AUTHENTICATOR.getKey(), SimpleAuthenticator.class.getName());

    Authenticator authenticator = loader.getAuthenticator(new PromiscuousAuthenticator());
    assertNotNull(authenticator);
    assertEquals(SimpleAuthenticator.class, authenticator.getClass());
  }

  @Test
  public void testMakeNullAuthenticator() throws Exception {
    Authenticator a = loader.makeAuthenticator(null);
    assertTrue(a instanceof PromiscuousAuthenticator);
  }

  @Test
  public void testMakeOneUserAuthenticator() throws Exception {
    Authenticator a = loader.makeAuthenticator("bob:uncle");
    assertTrue(a instanceof OneUserAuthenticator);
    OneUserAuthenticator oua = (OneUserAuthenticator) a;
    assertEquals("bob", oua.getUser());
    assertEquals("uncle", oua.getPassword());
  }

  @Test
  public void testMakeMultiUserAuthenticator() throws Exception {
    final String passwordFilename = "testpasswd";
    File passwd = new File(passwordFilename);
    passwd.createNewFile();
    Authenticator a = loader.makeAuthenticator(passwordFilename);
    assertTrue(a instanceof MultiUserAuthenticator);
    passwd.delete();
  }


  @Test
  public void noContentFilter() throws Exception {
    ContentFilter filter = loader.loadContentFilter();
    assertNull(filter);
  }

  @Test
  public void haveContentFilter() throws Exception {
    testProperties.setProperty(ConfigurationParameter.CONTENT_FILTER.getKey(), TestContentFilter.class.getName());

    ContentFilter filter = loader.loadContentFilter();
    assertNotNull(filter);
    assertEquals(TestContentFilter.class, filter.getClass());
  }

  @Test
  public void testWikiPageFactoryCreation() throws Exception {
    testProperties.setProperty(ConfigurationParameter.WIKI_PAGE_FACTORIES.getKey(), FooWikiPageFactory.class.getName());

    FileSystemPageFactory wikiPageFactory = mock(FileSystemPageFactory.class);
    loader.loadWikiPageFactories(wikiPageFactory);
    verify(wikiPageFactory).registerWikiPageFactory(any(FooWikiPageFactory.class));
  }

  @Test
  public void testSlimTablesCreation() throws PluginException {
    SlimTableFactory slimTableFactory = new SlimTableFactory();
    testProperties.setProperty(ConfigurationParameter.SLIM_TABLES.getKey(), "test:" + TestSlimTable.class.getName());
    loader.loadSlimTables(slimTableFactory);

    HtmlTable table = makeMockTable("test");
    SlimTable slimTable = slimTableFactory.makeSlimTable(table, "foo", new SlimTestContextImpl());
    assertSame(TestSlimTable.class, slimTable.getClass());
  }

  @Test
  public void testSlimTablesWithColonCreation() throws PluginException {
    testProperties.setProperty(ConfigurationParameter.SLIM_TABLES.getKey(), "test::" + TestSlimTable.class.getName());
    SlimTableFactory slimTableFactory = new SlimTableFactory();
    loader.loadSlimTables(slimTableFactory);

    HtmlTable table = makeMockTable("test:");
    SlimTable slimTable = slimTableFactory.makeSlimTable(table, "foo", new SlimTestContextImpl());
    assertSame(TestSlimTable.class, slimTable.getClass());
  }

  @Test
  public void testCustomComparatorsCreation() throws PluginException {
    CustomComparatorRegistry customComparatorRegistry = new CustomComparatorRegistry();
    testProperties.setProperty(ConfigurationParameter.CUSTOM_COMPARATORS.getKey(), "test:" + TestCustomComparator.class.getName());
    loader.loadCustomComparators(customComparatorRegistry);

    CustomComparator customComparator = customComparatorRegistry.getCustomComparatorForPrefix("test");
    assertNotNull(customComparator);
    assertTrue(customComparator instanceof TestCustomComparator);
  }

  @Test
  public void testTestSystemCreation() throws PluginException {
    testProperties.setProperty(ConfigurationParameter.TEST_SYSTEMS.getKey(), "foo:" + FooTestSystemFactory.class.getName());
    TestSystemFactoryRegistry registrar = mock(TestSystemFactoryRegistry.class);
    loader.loadTestSystems(registrar);

    verify(registrar).registerTestSystemFactory(eq("foo"), any(TestSystemFactory.class));
  }

  private HtmlTable makeMockTable(String tableIdentifier) {
    // Create just enough "table" to test if
    TableTag tableTag = new TableTag();
    TableRow tableRow = new TableRow();
    TableColumn tableColumn = new TableColumn();
    tableColumn.setChildren(new NodeList(new TextNode(tableIdentifier)));
    tableRow.setChildren(new NodeList(tableColumn));
    tableTag.setChildren(new NodeList(tableRow));
    return new HtmlTable(tableTag);
  }

  public static class TestContentFilter implements ContentFilter {
    public TestContentFilter(Properties p) {
      p.propertyNames();
    }

    public boolean isContentAcceptable(String content, String page) {
      return false;
    }
  }

  static public class DummyPlugin {

    public static void registerResponders(ResponderFactory factory) {
      factory.addResponder("custom1", WikiPageResponder.class);
      factory.addResponder("custom2", EditResponder.class);
    }

    public static void registerSymbolTypes(SymbolProvider provider) {
      provider.add(new Today());
    }
  }

  static public class InstantiableDummyPlugin {

    public final ComponentFactory componentFactory;

    public InstantiableDummyPlugin(ComponentFactory componentFactory) {
      this.componentFactory = componentFactory;
    }

    public void registerResponders(ResponderFactory factory) {
      factory.addResponder(componentFactory.getProperty("responderName"), WikiPageResponder.class);
    }

  }

  public static class TestSlimTable extends SlimTable {

    public TestSlimTable(Table table, String id, SlimTestContext testContext) {
      super(table, id, testContext);
    }

    @Override
    protected String getTableType() {
      return null;
    }

    @Override
    public List<SlimAssertion> getAssertions() {
      return null;
    }
  }
  
  public static class TestCustomComparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
      return false;
    }
  }
  
  public static class FooTestSystemFactory implements TestSystemFactory {
    @Override
    public TestSystem create(Descriptor descriptor) throws IOException {
      return null;
    }
  }

  public static class FooWikiPageFactory implements WikiPageFactory {

    @Override
    public WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource) {
      return null;
    }

    @Override
    public boolean supports(File path) {
      return false;
    }
  }
}
