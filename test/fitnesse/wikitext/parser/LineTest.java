package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class LineTest {
    @Test public void scansHeaders() {
        ParserTestHelper.assertScansTokenType("!1 some text\n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!2 \n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!3 text\n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!4 text\n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!5 text\n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!6 text\n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!3text\n", "HeaderLine", true);
        ParserTestHelper.assertScansTokenType("!0 text\n", "HeaderLine", false);
        ParserTestHelper.assertScansTokenType("!7 text\n", "HeaderLine", false);
    }

    @Test public void translatesHeaders() {
        for (int i = 1; i < 7; i++)
            ParserTestHelper.assertTranslatesTo("!" + i + " some text", "<h" + i + " " +
                "id=\"0\">some text</h" + i + ">" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("atstart\n!1 text\n", "atstart<br/><h1 " +
          "id=\"0\">text</h1>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("|!1 text|\n", ParserTestHelper.tableWithCell("<h1 " +
          "id=\"0\">text</h1>"));
        ParserTestHelper.assertTranslatesTo("| !1 text|\n", ParserTestHelper.tableWithCell("<h1" +
          " id=\"0\">text</h1>"));
        ParserTestHelper.assertTranslatesTo("|a|!1 text|\n", ParserTestHelper.tableWithCells(new
          String[] {"a", "<h1 id=\"0\">text</h1>"}));
    }

    @Test public void doesNotTranslateHeaders() {
        ParserTestHelper.assertTranslatesTo("notstart !1 text\n", "notstart !1 text<br/>");
        ParserTestHelper.assertTranslatesTo("|!1 text\n", "|!1 text<br/>");
    }

    @Test public void scansCenters() {
        ParserTestHelper.assertScansTokenType("!c text", "CenterLine", true);
        ParserTestHelper.assertScansTokenType("!C text", "CenterLine", true);
        ParserTestHelper.assertScansTokenType("!ctext", "CenterLine", true);
        ParserTestHelper.assertScansTokenType(" !c text\n", "CenterLine", false);
    }

    @Test public void translatesCenters() {
        ParserTestHelper.assertTranslatesTo("!c some text", "<center>some text</center>");
    }

    @Test public void scansNotes() {
        ParserTestHelper.assertScansTokenType("!note some note\n", "NoteLine", true);
        ParserTestHelper.assertScansTokenType("! note some note\n", "NoteLine", false);
    }

    @Test public void translatesNotes() {
        ParserTestHelper.assertTranslatesTo("!note some note", "<p class=\"note\">some note</p>" + HtmlElement.endl);
    }

    @Test public void translatesMetas() {
        ParserTestHelper.assertTranslatesTo("!meta stuff", "<span class=\"meta\">stuff</span>");
    }

    @Test public void translatesCombined() {
        ParserTestHelper.assertTranslatesTo("!1 !c stuff", "<h1 " +
          "id=\"0\"><center>stuff</center></h1>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("!1 !c stuff\n", "<h1 id=\"0\"><center>stuff</center></h1>"
          + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("!1 !c '''stuff'''\n", "<h1 " +
          "id=\"0\"><center><b>stuff</b></center></h1>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("!1 !c stuff\nmore", "<h1 id=\"0\"" +
          "><center>stuff</center></h1>" + HtmlElement.endl + "more");
    }
}
