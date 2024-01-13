package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.ParsingPage;
import org.junit.Test;

import static fitnesse.wikitext.parser.ParserTestHelper.assertParses;
import static org.junit.Assert.assertEquals;

public class DefineTest {
  @Test
  public void scansDefine() {
    ParserTestHelper.assertScansTokenType("!define x {y}", "Define", true);
    ParserTestHelper.assertScansTokenType("|!define x {y}|/n", "Define", true);
  }

  @Test
  public void parsesDefine() {
    assertParses("!define x {y}", "SymbolList[Define[Text, Text]]");
    assertParses("!define x {y" /* eof */, "SymbolList[Define[Text, Text]]");
    assertParses("!define x" /* eof */, "SymbolList[Define[Text, Text]]");
  }

  @Test public void parsesMissingBraces() {
    assertParses("!define x +1 stuff", "SymbolList[Text, Whitespace, Text, Whitespace, Delta, Whitespace, Text]");
  }

  @Test
  public void translatesDefines() {
    assertTranslatesDefine("!define x {y}", "x=y");
    assertTranslatesDefine("!define BoBo {y}", "BoBo=y");
    assertTranslatesDefine("!define BoBo  {y}", "BoBo=y");
    assertTranslatesDefine("!define x {}", "x=");
    assertTranslatesDefine("!define x_x {y}", "x_x=y");
    assertTranslatesDefine("!define x.x {y}", "x.x=y");
    assertTranslatesDefine("!define x (y)", "x=y");
    assertTranslatesDefine("!define x [y]", "x=y");
    assertTranslatesDefine("!define x {''y''}", "x=''y''");
    assertTranslatesDefine("!define x", "x=");
    ParserTestHelper.assertTranslatesTo("|!define x {y}", "|!define x {y}");
  }

  @Test
  public void definesValues() {
    assertDefinesValue("!define x {y}", "x", "y");
    assertDefinesValue("|!define x {y}|\n", "x", "y");
    assertDefinesValue("{{{\n!define notDefined {y}\n}}}", "notDefined", "*nothing*");
    //todo: move to variableTest?
    //assertDefinesValue("!define x {''y''}", "x", "<i>y</i>");
    //assertDefinesValue("!define x {!note y\n}", "x", "<span class=\"note\">y</span><br/>");
    //assertDefinesValue("!define z {y}\n!define x {${z}}", "x", "y");
    //assertDefinesValue("!define z {''y''}\n!define x {${z}}", "x", "<i>y</i>");
    //assertDefinesValue("!define z {y}\n!define x {''${z}''}", "x", "<i>y</i>");
  }

  @Test
  public void definesTable() {
    assertTranslatesDefine("!define x {|a|b|c|}", "x=|a|b|c|");
  }

  @Test
  public void definesTwoTables() {
    WikiPage pageOne = new TestRoot().makePage("PageOne");
    ParserTestHelper.assertTranslatesTo(pageOne,
      "!define x {|a|b|c|}\n!define y {|d|e|f|}",
      MakeDefinition("x=|a|b|c|") + HtmlElement.endl + "<br/>"
        + MakeDefinition("y=|d|e|f|") + HtmlElement.endl);
  }

  @Test
  public void CopiesSymbolValueWhenParsed() {
    assertDefinesValue("!define y {yvalue}\n!define x y\n", "x", "yvalue");
  }

  private void assertDefinesValue(String input, String name, String definedValue) {
    WikiPage pageOne = new TestRoot().makePage("PageOne", input);
    ParsingPage page = new ParsingPage(new WikiSourcePage(pageOne));
    Parser.make(page, input).parse();
    assertEquals(definedValue, page.findVariable(name).orElse("*nothing*"));
  }

  private void assertTranslatesDefine(String input, String definition) {
    WikiPage pageOne = new TestRoot().makePage("PageOne");
    ParserTestHelper.assertTranslatesTo(pageOne, input, MakeDefinition(definition) + HtmlElement.endl);
  }

  private String MakeDefinition(String definition) {
    return "<span class=\"meta\">variable defined: " + definition + "</span>";
  }
}
