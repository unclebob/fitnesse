// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.util.List;
import java.util.Properties;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.testsystems.slim.HtmlTable;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.tables.Assertion;
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
import util.RegexTestCase;

public class ComponentFactoryTest extends RegexTestCase {
  private Properties testProperties;
  private ComponentFactory factory;
  private SymbolProvider testProvider;

  @Override
  public void setUp() throws Exception {
    testProperties = new Properties();
    testProvider = new SymbolProvider(new SymbolType[] {});
    factory = new ComponentFactory(testProperties);
  }

  public void testAddPlugins() throws Exception {
    testProperties.setProperty(ComponentFactory.PLUGINS, DummyPlugin.class.getName());

    FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory();
    ResponderFactory responderFactory = new ResponderFactory(".");

    assertMatch("!today", false);

    String output = factory.loadPlugins(responderFactory, testProvider);

    assertSubString(DummyPlugin.class.getName(), output);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
    assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
    assertMatch("!today", true);
  }

    private void assertMatch(String input, boolean expected) {
        SymbolMatch match = new ParseSpecification().provider(testProvider).findMatch(new ScanString(input, 0), 0, new SymbolStream());
        assertEquals(match.isMatch(), expected);
    }

    public void testAddResponderPlugins() throws Exception {
    String respondersValue = "custom1:" + WikiPageResponder.class.getName() + ",custom2:" + EditResponder.class.getName();
    testProperties.setProperty(ComponentFactory.RESPONDERS, respondersValue);

    ResponderFactory responderFactory = new ResponderFactory(".");
    String output = factory.loadResponders(responderFactory);

    assertSubString("custom1:" + WikiPageResponder.class.getName(), output);
    assertSubString("custom2:" + EditResponder.class.getName(), output);

    assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
    assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
  }

  public void testWikiWidgetPlugins() throws Exception {
    String symbolValues = Today.class.getName();
    testProperties.setProperty(ComponentFactory.SYMBOL_TYPES, symbolValues);

    String output = factory.loadSymbolTypes(testProvider);

    assertSubString(Today.class.getName(), output);

    assertMatch("!today", true);
  }

  public void testAuthenticatorDefaultCreation() throws Exception {
    Authenticator authenticator = factory.getAuthenticator(new PromiscuousAuthenticator());
    assertNotNull(authenticator);
    assertEquals(PromiscuousAuthenticator.class, authenticator.getClass());
  }

  public void testAuthenticatorCustomCreation() throws Exception {
    testProperties.setProperty(ComponentFactory.AUTHENTICATOR, SimpleAuthenticator.class.getName());

    Authenticator authenticator = factory.getAuthenticator(new PromiscuousAuthenticator());
    assertNotNull(authenticator);
    assertEquals(SimpleAuthenticator.class, authenticator.getClass());
  }

  public void testContentFilterCreation() throws Exception {
    assertEquals("", factory.loadContentFilter());
    assertEquals(null, SaveResponder.contentFilter);

    testProperties.setProperty(ComponentFactory.CONTENT_FILTER, TestContentFilter.class.getName());

    String content = factory.loadContentFilter();
    assertEquals("\tContent filter installed: " + SaveResponder.contentFilter.getClass().getName() + "\n", content);
    assertNotNull(SaveResponder.contentFilter);
    assertEquals(TestContentFilter.class, SaveResponder.contentFilter.getClass());
  }

  public void testSlimTablesCreation() throws ClassNotFoundException {
    testProperties.setProperty(ComponentFactory.SLIM_TABLES, "test:" + TestSlimTable.class.getName());
    String content = factory.loadSlimTables();

    assertTrue(content.contains("test:"));
    assertTrue(content.contains("TestSlimTable"));

    HtmlTable table = makeMockTable("test");
    SlimTable slimTable = new SlimTableFactory().makeSlimTable(table, "foo", new SlimTestContextImpl());
    assertSame(TestSlimTable.class, slimTable.getClass());
  }

  public void testSlimTablesWithColonCreation() throws ClassNotFoundException {
    testProperties.setProperty(ComponentFactory.SLIM_TABLES, "test::" + TestSlimTable.class.getName());
    String content = factory.loadSlimTables();

    assertTrue(content.contains("test:"));
    assertTrue(content.contains("TestSlimTable"));

    HtmlTable table = makeMockTable("test:");
    SlimTable slimTable = new SlimTableFactory().makeSlimTable(table, "foo", new SlimTestContextImpl());
    assertSame(TestSlimTable.class, slimTable.getClass());
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
    public List<Assertion> getAssertions() {
      return null;
    }
  }
}
