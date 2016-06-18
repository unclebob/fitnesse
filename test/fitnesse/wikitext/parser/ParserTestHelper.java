package fitnesse.wikitext.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.BaseWikitextPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class ParserTestHelper {
  public static final String newLineRendered = "<br/>";

  public static void assertScans(String input, String expected) {
    Scanner scanner = new Scanner(new TestSourcePage(), input);
    assertScans(expected, scanner);
  }

  public static void assertScansTokenType(String input, String expected, boolean found) {
    Scanner scanner = new Scanner(new TestSourcePage(), input);
    while (true) {
      scanner.moveNext();
      if (scanner.isEnd()) break;
      if (scanner.getCurrent().getType().toString().equals(expected)) {
        assertTrue(found);
        return;
      }
    }
    assertTrue(!found);
  }

  public static void assertScans(String expected, Scanner scanner) {
    StringBuilder result = new StringBuilder();
    while (true) {
      scanner.moveNext();
      if (scanner.isEnd()) break;
      if (result.length() > 0) result.append(",");
      Symbol current = scanner.getCurrent();
      String name = current.getType().toString();
      result.append(name);
      String content = current.getContent();
      if (!content.isEmpty()) result.append("=").append(content);
    }
    assertEquals(expected, result.toString());
  }

  public static void assertTranslatesTo(String input, String expected) {
    assertTranslatesTo(new TestSourcePage(), input, expected);
  }

  public static void assertTranslatesTo(WikiPage page, VariableSource variableSource, String expected) {
    assertEquals(expected, translateToHtml(page, page.getData().getContent(), variableSource));
  }

  public static void assertTranslatesTo(String input, VariableSource variableSource, String expected) {
    assertEquals(expected, translateToHtml(new WikiPageDummy(), input, variableSource));
  }

  public static void assertTranslatesTo(WikiPage page, String input, String expected) {
    assertEquals(expected, translateTo(page, input));
  }

  public static void assertTranslatesTo(SourcePage page, String input, String expected) {
    assertEquals(expected, translateTo(page, input));
    assertEquals("round trip", input, roundTrip(page, input));
  }

  public static void assertTranslatesTo(SourcePage page, String expected) {
    assertEquals(expected, translateTo(page));
  }

  public static void assertTranslatesTo(WikiPage page, String expected) {
    assertEquals(expected, translateTo(new WikiSourcePage(page)));
  }

  public static String translateTo(WikiPage page, String input) {
    ParsingPage.Cache cache = new ParsingPage.Cache();
    VariableSource variableSource = new CompositeVariableSource(cache, new BaseWikitextPage.ParentPageVariableSource(page));
    Symbol list = Parser.make(new ParsingPage(new WikiSourcePage(page), variableSource, cache), input).parse();
    return new HtmlTranslator(new WikiSourcePage(page), new ParsingPage(new WikiSourcePage(page))).translateTree(list);
  }

  public static String translateTo(SourcePage page, String input) {
    Symbol list = Parser.make(new ParsingPage(page), input).parse();
    return new HtmlTranslator(page, new ParsingPage(page)).translateTree(list);
  }

  public static String roundTrip(SourcePage page, String input) {
    Symbol list = Parser.make(new ParsingPage(page), input, SymbolProvider.refactoringProvider).parse();
    return new WikiTranslator(page).translateTree(list);
  }

  public static String translateToHtml(WikiPage page, String input, VariableSource variableSource) {
    ParsingPage.Cache cache = new ParsingPage.Cache();
    ParsingPage currentPage = new ParsingPage(new WikiSourcePage(page), new CompositeVariableSource(cache, variableSource), cache);
    Symbol list = Parser.make(currentPage, input, SymbolProvider.wikiParsingProvider).parse();
    return new HtmlTranslator(new WikiSourcePage(page), currentPage).translateTree(list);
  }

  public static String translateTo(WikiPage page) {
    return translateTo(new WikiSourcePage(page));
  }

  public static String translateTo(SourcePage page, VariableSource variableSource) {
    ParsingPage.Cache cache = new ParsingPage.Cache();
    return new HtmlTranslator(page, new ParsingPage(page)).translateTree(Parser.make(new ParsingPage(page, new CompositeVariableSource(cache, variableSource), cache), page.getContent(), SymbolProvider.wikiParsingProvider).parse());
  }

  public static String translateTo(SourcePage page) {
    return new HtmlTranslator(page, new ParsingPage(page)).translateTree(Parser.make(new ParsingPage(page), page.getContent()).parse());
  }

  public static void assertParses(String input, String expected) {
    Symbol result = parse(input);
    assertEquals(expected, serialize(result));
  }

  public static void assertParsesWithOffset(String input, String expected) {
    Symbol result = parse(input);
    assertEquals(expected, serializeWithOffset(result));
  }

  public static Symbol parse(final String input) {
    WikiPage page = new TestRoot().makePage("TestPage", input);
    return parse(page, input);
  }

  public static Symbol parse(WikiPage page) {
    return Parser.make(new ParsingPage(new WikiSourcePage(page)), page.getData().getContent()).parse();
  }

  public static Symbol parse(WikiPage page, String input) {
    return Parser.make(new ParsingPage(new WikiSourcePage(page)), input).parse();
  }

  public static String serialize(Symbol symbol) {
    StringBuilder result = new StringBuilder();
    result.append(symbol.getType() != null ? symbol.getType().toString() : "?no type?");
    int i = 0;
    for (Symbol child : symbol.getChildren()) {
      result.append(i == 0 ? "[" : ", ");
      result.append(serialize(child));
      i++;
    }
    if (i > 0) result.append("]");
    return result.toString();
  }

  public static String serializeWithOffset(Symbol symbol) {
    StringBuilder result = new StringBuilder();
    result.append(symbol.getType() != null ? symbol.getType().toString() : "?no type?")
            .append("<").append(symbol.getStartOffset()).append("..").append(symbol.getEndOffset()).append(">");
    int i = 0;
    for (Symbol child : symbol.getChildren()) {
      result.append(i == 0 ? "[" : ", ");
      result.append(serializeWithOffset(child));
      i++;
    }
    if (i > 0) result.append("]");
    return result.toString();
  }

  public static String serializeContent(Symbol symbol) {
    StringBuilder result = new StringBuilder();
    if (symbol.getContent() != null) result.append(symbol.getContent());
    for (Symbol child : symbol.getChildren()) result.append(serializeContent(child));
    return result.toString();
  }

  public static String metaHtml(String message) {
    return "<span class=\"meta\">" + message + "</span>";
  }

  public static String tableWithCell(String cellContent) {
      return tableWithCell(cellContent, false);
  }

  public static String tableWithCell(String cellContent, boolean hasRowTitle) {
    return tableWithCellAndRow(cellContent, "<tr" + (hasRowTitle?" class=\"rowTitle\"":"") + ">");
  }

  public static String tableWithCells(String[] cellContent) {
    StringBuilder cells = new StringBuilder();
    for (String cell : cellContent) {
      if (cells.length() > 0) cells.append("</td>").append(HtmlElement.endl).append("\t\t<td>");
      cells.append(cell);
    }
    return tableWithCellAndRow(cells.toString(), "<tr>");
  }

  public static String tableWithCellAndRow(String cellContent, String firstRow) {
    return nestedTableWithCellAndRow(cellContent, firstRow) + HtmlElement.endl;
  }

  public static String nestedTableWithCellAndRow(String cellContent, String firstRow) {
    StringBuilder tableWithCellAndRow = new StringBuilder();
    tableWithCellAndRow.append("<table>").append(HtmlElement.endl)
      .append("\t").append(firstRow).append(HtmlElement.endl)
      .append("\t\t<td>").append(cellContent).append("</td>").append(HtmlElement.endl)
      .append("\t</tr>").append(HtmlElement.endl).append("</table>");
    return tableWithCellAndRow.toString();
  }

}
