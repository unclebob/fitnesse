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
import fitnesse.responders.editing.SaveResponder;
import fitnesse.testrunner.TestSystemFactoryRegistrar;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
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
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wikitext.parser.ParseSpecification;
import fitnesse.wikitext.parser.ScanString;
import fitnesse.wikitext.parser.SymbolMatch;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolStream;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Today;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PluginsLoaderTest {
  private Properties testProperties;
  private PluginsLoader loader;
  private SymbolProvider testProvider;

  @Before
  public void setUp() throws Exception {
    testProperties = new Properties();
    testProvider = new SymbolProvider(new SymbolType[] {});
    loader = new PluginsLoader(new ComponentFactory(testProperties));
  }

  @Test
  public void testAddPlugins() throws Exception {
    testProperties.setProperty(ComponentFactory.PLUGINS, DummyPlugin.class.getName());

    FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory();
    ResponderFactory responderFactory = new ResponderFactory(".");

    assertMatch("!today", false);

    loader.loadPlugins(responderFactory, testProvider);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
    assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
    assertMatch("!today", true);
  }

  private void assertMatch(String input, boolean expected) {
    SymbolMatch match = new ParseSpecification().provider(testProvider).findMatch(new ScanString(input, 0), 0, new SymbolStream());
    assertEquals(match.isMatch(), expected);
  }

  @Test
  public void testAddResponderPlugins() throws Exception {
    String respondersValue = "custom1:" + WikiPageResponder.class.getName() + ",custom2:" + EditResponder.class.getName();
    testProperties.setProperty(ComponentFactory.RESPONDERS, respondersValue);

    ResponderFactory responderFactory = new ResponderFactory(".");
    loader.loadResponders(responderFactory);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
    assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
  }

  @Test
  public void testWikiWidgetPlugins() throws Exception {
    String symbolValues = Today.class.getName();
    testProperties.setProperty(ComponentFactory.SYMBOL_TYPES, symbolValues);

    loader.loadSymbolTypes(testProvider);

    assertMatch("!today", true);
  }

  @Test
  public void testAuthenticatorDefaultCreation() throws Exception {
    Authenticator authenticator = loader.getAuthenticator(new PromiscuousAuthenticator());
    assertNotNull(authenticator);
    assertEquals(PromiscuousAuthenticator.class, authenticator.getClass());
  }

  @Test
  public void testAuthenticatorCustomCreation() throws Exception {
    testProperties.setProperty(ComponentFactory.AUTHENTICATOR, SimpleAuthenticator.class.getName());

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
  public void testContentFilterCreation() throws Exception {
    loader.loadContentFilter();
    assertEquals(null, SaveResponder.contentFilter);

    testProperties.setProperty(ComponentFactory.CONTENT_FILTER, TestContentFilter.class.getName());

    loader.loadContentFilter();
    assertNotNull(SaveResponder.contentFilter);
    assertEquals(TestContentFilter.class, SaveResponder.contentFilter.getClass());
  }

  @Test
  public void testSlimTablesCreation() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    testProperties.setProperty(ComponentFactory.SLIM_TABLES, "test:" + TestSlimTable.class.getName());
    loader.loadSlimTables();

    HtmlTable table = makeMockTable("test");
    SlimTable slimTable = new SlimTableFactory().makeSlimTable(table, "foo", new SlimTestContextImpl());
    assertSame(TestSlimTable.class, slimTable.getClass());
  }

  @Test
  public void testSlimTablesWithColonCreation() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    testProperties.setProperty(ComponentFactory.SLIM_TABLES, "test::" + TestSlimTable.class.getName());
    loader.loadSlimTables();

    HtmlTable table = makeMockTable("test:");
    SlimTable slimTable = new SlimTableFactory().makeSlimTable(table, "foo", new SlimTestContextImpl());
    assertSame(TestSlimTable.class, slimTable.getClass());
  }

  @Test
  public void testCustomComparatorsCreation() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    testProperties.setProperty(ComponentFactory.CUSTOM_COMPARATORS, "test:" + TestCustomComparator.class.getName());
    loader.loadCustomComparators();

    CustomComparator customComparator = CustomComparatorRegistry.getCustomComparatorForPrefix("test");
    assertNotNull(customComparator);
    assertTrue(customComparator instanceof TestCustomComparator);
  }

  @Test
  public void testTestSystemCreation() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
    testProperties.setProperty(ComponentFactory.TEST_SYSTEMS, "foo:" + FooTestSystemFactory.class.getName());
    TestSystemFactoryRegistrar registrar = mock(TestSystemFactoryRegistrar.class);
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

  static class DummyPlugin {

    public static void registerResponders(ResponderFactory factory) {
      factory.addResponder("custom1", WikiPageResponder.class);
      factory.addResponder("custom2", EditResponder.class);
    }

    public static void registerSymbolTypes(SymbolProvider provider) {
      provider.add(new Today());
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
}
