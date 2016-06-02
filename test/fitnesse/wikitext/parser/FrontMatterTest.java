package fitnesse.wikitext.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FrontMatterTest {

  @Test
  public void parsesEmptyFrontMatter() {
    ParserTestHelper.assertParses("---\n---\n", "SymbolList[FrontMatter[SymbolList]]");
  }

  @Test
  public void notAValidFrontMatter() {
    ParserTestHelper.assertParses("---\ntest---\n", "SymbolList[Strike[SymbolList[Text, Newline, Text]], Text, Newline]");
  }

  @Test
  public void parseFrontMatterWithText() {
    ParserTestHelper.assertParses("---\ntest\n---\nWikiText", "SymbolList[FrontMatter[SymbolList[SymbolList[Text, Text]]], WikiWord]");
  }

  @Test
  public void parseFrontMatterWithKeyValueText() {
    ParserTestHelper.assertParses("---\ntest: value with whitespace\n---\n", "SymbolList[FrontMatter[SymbolList[SymbolList[Text, Text]]]]");
  }

  @Test
  public void parseFrontMatterWithSymbolicLinkText() {
    ParserTestHelper.assertParses("---\nsymbolic-links:\n  pageName: .FrontPage\n---\n", "SymbolList[FrontMatter[SymbolList[SymbolList[Text, Text, SymbolList[Text, Text]]]]]");
  }

  @Test
  public void readFrontMatter() {
    final Symbol symbols = ParserTestHelper.parse("---\nsymbolic-links:\n  pageName: .FrontPage\n---\n");
    Symbol frontMatter = symbols.getChildren().get(0);
    Symbol symlinks = frontMatter.getChildren().get(0).getChildren().get(0);
    Symbol firstSymlink = symlinks.getChildren().get(2);

    assertTrue(frontMatter.isType(FrontMatter.symbolType));
    assertEquals("symbolic-links", symlinks.getChildren().get(0).getContent());
    assertEquals("pageName", firstSymlink.getChildren().get(0).getContent());
    assertEquals(".FrontPage", firstSymlink.getChildren().get(1).getContent());
  }
}
