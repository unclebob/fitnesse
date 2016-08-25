package fitnesse.wikitext.parser;

import org.junit.Test;

import fitnesse.wiki.WikiPageDummy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FrontMatterTest {

  @Test
  public void parsesEmptyFrontMatter() {
    assertParses(
        "---\n" +
        "---\n",
      "SymbolList[FrontMatter]");
  }

  @Test
  public void parsesSecurityProperty() {
    assertParses(
      "---\n" +
        "LastModified: 20160706000400\n" +
        "secure-read\n" +
        "---\n",
      "SymbolList[FrontMatter[KeyValue[Text, Text], KeyValue[Text, Text]]]");
  }

  @Test
  public void notAValidFrontMatter() {
    assertParses(
        "---\n" +
        "test---\n",
      "SymbolList[Text]");
  }

  @Test
  public void parseFrontMatterWithText() {
    assertParses(
        "---\n" +
        "test\n" +
        "---\n" +
        "WikiText",
      "SymbolList[FrontMatter[KeyValue[Text, Text]], Text]");
  }

  @Test
  public void parseFrontMatterWithKeyValueText() {
    assertParses(
        "---\n" +
        "test: value with whitespace\n" +
        "---\n",
      "SymbolList[FrontMatter[KeyValue[Text, Text]]]");
  }

  @Test
  public void parseFrontMatterWithSymbolicLinkText() {
    assertParses(
        "---\n" +
        "symbolic-links:\n" +
        "  pageName: .FrontPage\n" +
        "  pageName: .FrontPage\n" +
        "---\n",
      "SymbolList[FrontMatter[KeyValue[Text, Text, KeyValue[Text, Text], KeyValue[Text, Text]]]]");
  }

  @Test
  public void thirdDashLineIsJustText() {
    assertParses(
        "---\n" +
        "Test\n" +
        "---\n" +
        "\n" +
        "---\n",
      "SymbolList[FrontMatter[KeyValue[Text, Text]], Text, Text]");
  }

  @Test
  public void readFrontMatter() {
    final Symbol symbols = parse(
        "---\n" +
        "symbolic-links:\n" +
        "  pageName: .FrontPage\n" +
        "---\n");
    Symbol frontMatter = symbols.getChildren().get(0);
    Symbol symlinks = frontMatter.getChildren().get(0);
    Symbol firstSymlink = symlinks.getChildren().get(2);

    assertTrue(frontMatter.isType(FrontMatter.symbolType));
    assertEquals("symbolic-links", symlinks.getChildren().get(0).getContent());
    assertEquals("pageName", firstSymlink.getChildren().get(0).getContent());
    assertEquals(".FrontPage", firstSymlink.getChildren().get(1).getContent());
  }

  @Test
  public void readFrontMatterWithManySpacesBeforeChildEntry() {
    final Symbol symbols = parse(
      "---\n" +
        "symbolic-links:\n" +
        "             pageName: .FrontPage\n" +
        "---\n");
    Symbol frontMatter = symbols.getChildren().get(0);
    Symbol symlinks = frontMatter.getChildren().get(0);
    Symbol firstSymlink = symlinks.getChildren().get(2);

    assertTrue(frontMatter.isType(FrontMatter.symbolType));
    assertEquals("symbolic-links", symlinks.getChildren().get(0).getContent());
    assertEquals("pageName", firstSymlink.getChildren().get(0).getContent());
    assertEquals(".FrontPage", firstSymlink.getChildren().get(1).getContent());
  }

  @Test
  public void parsesWithEmptyLines() {
    final Symbol symbols = parse("---\n" +
      "\n" +
      "---\n" +
      "\n");
    Symbol frontMatter = symbols.getChildren().get(0);
    assertTrue(frontMatter.isType(FrontMatter.symbolType));
    assertTrue(frontMatter.getChildren().isEmpty());
  }

  @Test
  public void readUrlInFrontMatter() {
    final Symbol symbols = parse(
      "---\n" +
        "symbolic-links:\n" +
        "  pageName: file://test\n" +
        "---\n");
    Symbol frontMatter = symbols.getChildren().get(0);
    Symbol symlinks = frontMatter.getChildren().get(0);
    Symbol firstSymlink = symlinks.getChildren().get(2);

    assertTrue(frontMatter.isType(FrontMatter.symbolType));
    assertEquals("symbolic-links", symlinks.getChildren().get(0).getContent());
    assertEquals("pageName", firstSymlink.getChildren().get(0).getContent());
    assertEquals("file://test", firstSymlink.getChildren().get(1).getContent());
  }

  public void assertParses(String input, String expected) {
    Symbol result = parse(input);
    assertEquals(expected, ParserTestHelper.serialize(result));
  }

  private Symbol parse(final String input) {
    return Parser.make(new ParsingPage(new WikiSourcePage(new WikiPageDummy())), input,
      new SymbolProvider(new SymbolType[] { FrontMatter.symbolType, SymbolType.Text })).parse();
  }
}
