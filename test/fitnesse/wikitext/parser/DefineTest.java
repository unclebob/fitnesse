package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
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
    checkMultipleTables("", 2);
  }

  @Test
  public void definesManyTablesCurlyOnEndOfLine() {
    checkMultipleTables("", 15);
  }

  @Test
  public void definesManyTablesCurlyOnEndOfLineSpaceBefore() {
    checkMultipleTables(" ", 15);
  }

  @Test
  public void definesManyTablesCurlyOnEndOfLineSpacesBefore() {
    checkMultipleTables("   ", 15);
  }

  @Test
  public void definesManyTablesCurlyAtStartOfLine() {
    checkMultipleTables("\n", 15);
  }

  private void checkMultipleTables(String postfix, int count) {
    WikiPage pageOne = new TestRoot().makePage("PageOne");
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        sb.append("\n");
        sb2.append("<br/>");
      }
      sb.append(createTableDefine("x", i, postfix));
      sb2.append(MakeDefinition(createExpectedDefinition("x", i, postfix)));
      sb2.append(HtmlElement.endl);
    }
    ParserTestHelper.assertTranslatesTo(pageOne, sb.toString(), sb2.toString());
  }

  private String createTableDefine(String name, int index, String postfix) {
      return String.format("!define %s%s {|a|b|c|\n|a|b|%s}", name, index, postfix);
  }
      private String createExpectedDefinition(String name, int index, String postfix) {
    return String.format("%s%s=|a|b|c|\n|a|b|%s", name, index, postfix);
  }

  @Test
  public void CopiesSymbolValueWhenParsed() {
    assertDefinesValue("!define y {yvalue}\n!define x y\n", "x", "yvalue");
  }

  private void assertDefinesValue(String input, String name, String definedValue) {
    WikiPage pageOne = new TestRoot().makePage("PageOne", input);
    ParsingPage page = new ParsingPage(new WikiSourcePage(pageOne));
    Parser.make(page, input).parse();
    assertEquals(definedValue, page.findVariable(name).getValue());
  }

  private void assertTranslatesDefine(String input, String definition) {
    WikiPage pageOne = new TestRoot().makePage("PageOne");
    ParserTestHelper.assertTranslatesTo(pageOne, input, MakeDefinition(definition) + HtmlElement.endl);
  }

  private String MakeDefinition(String definition) {
    return "<span class=\"meta\">variable defined: " + definition + "</span>";
  }

}
