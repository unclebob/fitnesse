package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class LineTest {
    @Test public void scansHeaders() {
        ParserTestHelper.assertScans("!1 some text\n", "HeaderLine=!1,Whitespace= ,Text=some,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!2 \n", "HeaderLine=!2,Whitespace= ,Newline=\n");
        ParserTestHelper.assertScans("!3 text\n", "HeaderLine=!3,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!4 text\n", "HeaderLine=!4,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!5 text\n", "HeaderLine=!5,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!6 text\n", "HeaderLine=!6,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!3text\n", "HeaderLine=!3,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!0 text\n", "Text=!0,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!7 text\n", "Text=!7,Whitespace= ,Text=text,Newline=\n");
    }

    @Test public void translatesHeaders() {
        for (int i = 1; i < 7; i++)
            ParserTestHelper.assertTranslatesTo("!" + i + " some text", "<h" + i + ">some text</h" + i + ">" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("atstart\n!1 text\n", "atstart<br/><h1>text</h1>" + HtmlElement.endl + "<br/>");
        ParserTestHelper.assertTranslatesTo("|!1 text|\n", ParserTestHelper.tableWithCell("<h1>text</h1>"));
        ParserTestHelper.assertTranslatesTo("| !1 text|\n", ParserTestHelper.tableWithCell("<h1>text</h1>"));
        ParserTestHelper.assertTranslatesTo("|a|!1 text|\n", ParserTestHelper.tableWithCells(new String[] {"a", "<h1>text</h1>"}));
    }

    @Test public void doesNotTranslateHeaders() {
        ParserTestHelper.assertTranslatesTo("notstart !1 text\n", "notstart !1 text<br/>");
        ParserTestHelper.assertTranslatesTo("|!1 text\n", "|!1 text<br/>");
    }

    @Test public void scansCenters() {
        ParserTestHelper.assertScans("!c some text\n", "CenterLine=!c,Whitespace= ,Text=some,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!C more text\n", "CenterLine=!C,Whitespace= ,Text=more,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!ctext\n", "CenterLine=!c,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!c text\n", "CenterLine=!c,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans(" !c text\n", "Whitespace= ,CenterLine=!c,Whitespace= ,Text=text,Newline=\n");
        ParserTestHelper.assertScans("!c text", "CenterLine=!c,Whitespace= ,Text=text");
    }

    @Test public void translatesCenters() {
        ParserTestHelper.assertTranslatesTo("!c some text", "<center>some text</center>" + HtmlElement.endl);
    }

    @Test public void scansNotes() {
        ParserTestHelper.assertScans("!note some note\n", "NoteLine=!note,Whitespace= ,Text=some,Whitespace= ,Text=note,Newline=\n");
        ParserTestHelper.assertScans("! note some note\n", "Text=!,Whitespace= ,Text=note,Whitespace= ,Text=some,Whitespace= ,Text=note,Newline=\n");
    }

    @Test public void translatesNotes() {
        ParserTestHelper.assertTranslatesTo("!note some note", "<span class=\"note\">some note</span>");
    }

    @Test public void translatesMetas() {
        ParserTestHelper.assertTranslatesTo("!meta stuff", "<span class=\"meta\">stuff</span>");
    }
}
