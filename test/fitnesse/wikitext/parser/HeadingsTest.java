package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.shared.Names;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static fitnesse.wikitext.parser.ParserTestHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HeadingsTest {

  private final String LINE_SEPARATOR = "line.separator";
  private String originalLineSeparator;

  @Before
  public void setUpLineSeparator() {
    originalLineSeparator = System.getProperty(LINE_SEPARATOR);
    System.setProperty(LINE_SEPARATOR, "\n");
  }

  @After
  public void resetLineSeparator() {
    System.setProperty(LINE_SEPARATOR, originalLineSeparator);
  }

  @Test
  public void testConstructor_WhenCalled_ExpectedDefaultValues() {
    // arrange

    // act
    Headings headings = new Headings();

    // assert
    assertEquals("Headings", headings.toString());
    assertEquals(headings, headings.getWikiRule());
    assertEquals(headings, headings.getHtmlTranslation());
  }

  @Test
  public void testParse_whenFindHeadings_expectHeadingSymbol() {
    assertParses("!headings\n", "SymbolList[Headings, Newline]");
  }

  @Test
  public void testListStyle_whenWrongValue_expectDefault() {
    assertTrue(translateTo("!headings\n!1 hi").contains("list-style-type: decimal"));
    assertTrue(translateTo("!headings -style garbage\n!1 hi").contains("list-style-type: decimal"));
  }

  @Test
  public void whenEmtpyListExpectNull() {
    // arrange
    List<Symbol> headerLines = new LinkedList<>();

    // act
    Headings.HeadingContentBuilder builder = new Headings().new HeadingContentBuilder(headerLines,
      Headings.DEFAULT_STYLE);
    HtmlElement htmlElement = builder.htmlElements();

    // assert
    assertNotNull(htmlElement);
    assertTrue(htmlElement instanceof HtmlTag);
    assertEquals("div", ((HtmlTag) htmlElement).tagName());
  }

  @Test
  public void whenListContainsOneElementExpectOneListItem() {
    // arrange
    List<Symbol> parameters = new LinkedList<>();
    parameters.add(buildHeaderLine("1", "Text of Heading 1"));

    // act
    Headings.HeadingContentBuilder builder =
      new Headings().new HeadingContentBuilder(parameters, Headings.DEFAULT_STYLE);
    HtmlElement htmlElement = builder.htmlElements();

    // assert
    assertNotNull(htmlElement);
    assertTrue(htmlElement instanceof HtmlTag);
    assertTrue(Pattern.compile("<ol.*<li.*Text of Heading 1.*</li>.*</ol>", Pattern.DOTALL)
      .matcher(htmlElement.html()).find());
  }

  @Test
  public void whenListContainsTwoSiblingsExpectTwoListItems() {
    // arrange
    List<Symbol> parameters = new LinkedList<>();
    parameters.add(buildHeaderLine("1", "Text of Heading 1"));
    parameters.add(buildHeaderLine("1", "Text of Heading 2"));

    // act
    Headings.HeadingContentBuilder builder =
      new Headings().new HeadingContentBuilder(parameters, Headings.DEFAULT_STYLE);
    HtmlElement htmlElement = builder.htmlElements();

    // assert
    assertNotNull(htmlElement);
    assertTrue(htmlElement instanceof HtmlTag);
    assertTrue(Pattern.compile("<ol style.*<li class.*<a href.*Text of Heading 1</a>" +
      ".*</li>[\n\r\t ]*<li.*Text of Heading 2</a>.*</li>.*</ol>", Pattern.DOTALL)
      .matcher(htmlElement.html()).find());
  }

  @Test
  public void whenListContainsParentAndChildExpectOneListItemAndOneListItem() {
    // arrange
    List<Symbol> parameters = new LinkedList<>();
    parameters.add(buildHeaderLine("1", "Text of Heading 1"));
    parameters.add(buildHeaderLine("2", "Text of Heading 2"));

    // act
    Headings.HeadingContentBuilder builder =
      new Headings().new HeadingContentBuilder(parameters, Headings.DEFAULT_STYLE);
    HtmlElement htmlElement = builder.htmlElements();

    // assert
    assertNotNull(htmlElement);
    assertTrue(htmlElement instanceof HtmlTag);
    assertTrue(Pattern.compile("<ol style.*<li class.*<a href.*Text of Heading 1</a>.*</li>" +
      ".*<ol style.*<li class.*Text of Heading 2</a>.*</li>.*</ol>.*</ol>", Pattern.DOTALL)
      .matcher(htmlElement.html()).find());
  }

  @Test
  public void whenListContainsTwoSiblingsAndChildExpectTwoListItemsAndOneListItem() {
    // arrange
    List<Symbol> parameters = new LinkedList<>();
    parameters.add(buildHeaderLine("1", "Text of Heading 1"));
    parameters.add(buildHeaderLine("2", "Text of Heading 2"));
    parameters.add(buildHeaderLine("1", "Text of Heading 3"));

    // act
    Headings.HeadingContentBuilder builder =
      new Headings().new HeadingContentBuilder(parameters, Headings.DEFAULT_STYLE);
    HtmlElement htmlElement = builder.htmlElements();

    // assert
    assertNotNull(htmlElement);
    assertTrue(htmlElement instanceof HtmlTag);
    assertTrue(Pattern.compile("<ol style.*<li class.*<a href.*Text of Heading 1</a>.*</li>.*<ol" +
        " style.*>.*<li class.*<a href.*Text of Heading 2</a>.*</li>.*</ol>.*<li class" +
        ".*<a href.*Text of Heading 3</a>.*</li>.*</ol>",
      Pattern.DOTALL).matcher(htmlElement.html()).find());
  }

  @Test
  public void whenListContainsParentAndGrandChildExpectOneAndOneListItem() {
    // arrange
    List<Symbol> parameters = new LinkedList<>();
    parameters.add(buildHeaderLine("1", "Text of Heading 1"));
    parameters.add(buildHeaderLine("3", "Text of Heading 2"));

    // act
    Headings.HeadingContentBuilder builder =
      new Headings().new HeadingContentBuilder(parameters, Headings.DEFAULT_STYLE);
    HtmlElement htmlElement = builder.htmlElements();

    // assert
    assertNotNull(htmlElement);
    assertTrue(htmlElement instanceof HtmlTag);
    assertTrue(Pattern.compile("<ol style.*>.*<li class.*<a href.*Text of Heading 1</a>.*</li>" +
      ".*<ol style.*<ol style.*<li class.*<a href.*Text of Heading 2</a>.*</li>.*</ol>.*</ol>", Pattern.DOTALL)
      .matcher(htmlElement.html()).find());
  }

  @Test
  public void testOptionParser_whenNoOption_expectDefaults() {
    assertTrue(translateTo("!headings\n!1 hi").contains("list-style-type: decimal"));
  }

  @Test
  public void testOptionParser_whenOptionWithoutValue_expectOneKeyAndEmptyValue() {
    assertTrue(translateTo("!headings -style\n!1 hi").contains("list-style-type: decimal"));
  }

  @Test
  public void testOptionParser_whenOptionWithValue_expectOneKeyAndValue() {
    assertTrue(translateTo("!headings -style upper-roman\n!1 hi").contains("list-style-type: upper-roman"));
  }

  @Test
  public void translates() {
    TestRoot root = new TestRoot();
    WikiPage page = root.makePage("Page", "!headings\n!1 Title\n!2 Heading\n");
    assertTranslatesTo(page,
        "<div class=\"contents\">\n" +
        "\t<b>Contents:</b>\n" +
        "\t<ol style=\"list-style-type: decimal;\">\n" +
        "\t\t<li class=\"heading1\">\n" +
        "\t\t\t<a href=\"#0\">Title</a>\n" +
        "\t\t</li>\n" +
        "\t\t<ol style=\"list-style-type: decimal;\">\n" +
        "\t\t\t<li class=\"heading2\">\n" +
        "\t\t\t\t<a href=\"#1\">Heading</a>\n" +
        "\t\t\t</li>\n" +
        "\t\t</ol>\n" +
        "\t</ol>\n" +
        "</div>\n" +
        "<br/><h1 id=\"0\">Title</h1>\n" +
        "<h2 id=\"1\">Heading</h2>\n");
  }

  private Symbol buildHeaderLine(final String level, final String text) {
    Symbol symbol = new Symbol(HeaderLine.symbolType);
    symbol.putProperty(Names.LEVEL, level);
    symbol.putProperty(Names.ID, "5");
    Symbol symbolList = new Symbol(SymbolType.SymbolList);
    symbol.add(symbolList);
    symbolList.add(new Symbol(SymbolType.Text, text));
    return symbol;
  }

}
