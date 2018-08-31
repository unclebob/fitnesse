package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    // arrange
    Symbol current = new Symbol(SymbolType.Newline);
    TestSourcePage sourcePage = new TestSourcePage();
    String match = "!headings";
    Parser parser = Parser.make(new ParsingPage(sourcePage), match + "\n");
    Headings headings = new Headings();

    // act
    Maybe<Symbol> maybe = headings.parse(current, parser);

    // assert
    Symbol symbol = maybe.getValue();
    assertEquals(current, symbol);
    assertEquals(match, symbol.childAt(0).childAt(0).getContent());
  }

  @Test
  public void testToTarget_whenTranslate_expectHtml() {
    // arrange
    Symbol current = new Symbol(SymbolType.Newline);
    TestSourcePage sourcePage = new TestSourcePage();
    Translator translator = new HtmlTranslator(sourcePage, new ParsingPage(sourcePage));
    Headings headings = new Headings();

    // act
    String html = headings.toTarget(translator, current);

    // assert
    assertTrue(Pattern.compile("<div class=\"contents\">.*<b>Contents:</b>.*</div>", Pattern
      .DOTALL).matcher(html).find());
  }

  @Test
  public void testListStyle_whenAllowedValue_expectValid() {
    assertEquals(7, Headings.ListStyle.values().length);
    assertEquals(Headings.ListStyle.DECIMAL, Headings.ListStyle.byNameIgnoreCase("decimal"));
    assertEquals(Headings.ListStyle.DECIMAL_LEADING_ZERO,
      Headings.ListStyle.byNameIgnoreCase("decimal-leading-zero"));
    assertEquals(Headings.ListStyle.LOWER_ALPHA,
      Headings.ListStyle.byNameIgnoreCase("lower-alpha"));
    assertEquals(Headings.ListStyle.LOWER_ROMAN,
      Headings.ListStyle.byNameIgnoreCase("lower-roman"));
    assertEquals(Headings.ListStyle.NONE, Headings.ListStyle.byNameIgnoreCase("none"));
    assertEquals(Headings.ListStyle.UPPER_ALPHA,
      Headings.ListStyle.byNameIgnoreCase("upper-alpha"));
    assertEquals(Headings.ListStyle.UPPER_ROMAN,
      Headings.ListStyle.byNameIgnoreCase("upper-roman"));
  }

  @Test
  public void testListStyle_whenWrongValue_expectDefault() {
    assertEquals(Headings.ListStyle.DECIMAL, Headings.ListStyle.byNameIgnoreCase(null));
    assertEquals(Headings.ListStyle.DECIMAL, Headings.ListStyle.byNameIgnoreCase(""));
    assertEquals(Headings.ListStyle.DECIMAL, Headings.ListStyle.byNameIgnoreCase("XYZ"));
  }

  @Test
  public void whenEmtpyListExpectNull() {
    // arrange
    List<Symbol> headerLines = new LinkedList<>();

    // act
    Headings.HeadingContentBuilder builder = new Headings().new HeadingContentBuilder(headerLines,
      Headings.ListStyle.DECIMAL);
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
      new Headings().new HeadingContentBuilder(parameters, Headings.ListStyle.DECIMAL);
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
      new Headings().new HeadingContentBuilder(parameters, Headings.ListStyle.DECIMAL);
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
      new Headings().new HeadingContentBuilder(parameters, Headings.ListStyle.DECIMAL);
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
      new Headings().new HeadingContentBuilder(parameters, Headings.ListStyle.DECIMAL);
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
      new Headings().new HeadingContentBuilder(parameters, Headings.ListStyle.DECIMAL);
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
    // arrange
    Symbol current = new Symbol(Headings.symbolType);
    Symbol symbol = new Symbol(SymbolType.SymbolList);
    Headings.OptionParser optionParser = new Headings().new OptionParser(current, symbol);

    // act
    optionParser.parse();

    // assert
    assertFalse(current.hasProperty("STYLE"));
  }

  @Test
  public void testOptionParser_whenOptionWithoutValue_expectOneKeyAndNullValue() {
    // arrange
    Symbol current = new Symbol(Headings.symbolType);
    Symbol body = new Symbol(SymbolType.SymbolList);
    body.add(new Symbol(SymbolType.Whitespace));
    String style = "style";
    body.add(new Symbol(SymbolType.Text, "-" + style));
    Headings.OptionParser optionParser = new Headings().new OptionParser(current, body);

    // act
    optionParser.parse();

    // assert
    assertTrue(current.hasProperty(style.toUpperCase()));
    assertNull(current.getProperty(style.toUpperCase()));
  }

  @Test
  public void testOptionParser_whenOptionWithValue_expectOneKeyAndValue() {
    // arrange
    Symbol current = new Symbol(Headings.symbolType);
    Symbol body = new Symbol(SymbolType.SymbolList);
    body.add(new Symbol(SymbolType.Whitespace));
    String optionKey = "style";
    body.add(new Symbol(SymbolType.Text, "-" + optionKey));
    body.add(new Symbol(SymbolType.Whitespace));
    String optionValue = "decimal";
    body.add(new Symbol(SymbolType.Text, optionValue));
    Headings.OptionParser optionParser = new Headings().new OptionParser(current, body);

    // act
    optionParser.parse();

    // assert
    assertTrue(current.hasProperty(optionKey.toUpperCase()));
    assertEquals(optionValue, current.getProperty(optionKey.toUpperCase()));
  }

  private Symbol buildHeaderLine(final String level, final String text) {
    Symbol symbol = new Symbol(HeaderLine.symbolType);
    symbol.putProperty(LineRule.Level, level);
    Symbol symbolList = new Symbol(SymbolType.SymbolList);
    symbol.add(symbolList);
    symbolList.add(new Symbol(SymbolType.Text, text));
    return symbol;
  }

}
