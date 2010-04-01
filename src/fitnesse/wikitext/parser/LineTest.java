package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class LineTest {
    @Test public void scansHeaders() {
        ParserTest.assertScans("!1 some text\n", "HeaderLine=!1,Whitespace= ,Text=some,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!2 \n", "HeaderLine=!2,Whitespace= ,Newline=\n");
        ParserTest.assertScans("!3 text\n", "HeaderLine=!3,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!4 text\n", "HeaderLine=!4,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!5 text\n", "HeaderLine=!5,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!6 text\n", "HeaderLine=!6,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!3text\n", "HeaderLine=!3,Text=text,Newline=\n");
        ParserTest.assertScans("!0 text\n", "Text=!0,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!7 text\n", "Text=!7,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("not start !1 text\n", "Text=not,Whitespace= ,Text=start,Whitespace= ,Text=!1,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("at start\n!1 text\n", "Text=at,Whitespace= ,Text=start,Newline=\n,HeaderLine=!1,Whitespace= ,Text=text,Newline=\n");
    }

    @Test public void translatesHeaders() {
        for (int i = 1; i < 7; i++)
            ParserTest.assertTranslates("!" + i + " some text \n", "<h" + i + ">some text</h" + i + ">" + HtmlElement.endl);
    }

    @Test public void scansCenters() {
        ParserTest.assertScans("!c some text\n", "CenterLine=!c,Whitespace= ,Text=some,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!C more text\n", "CenterLine=!C,Whitespace= ,Text=more,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!ctext\n", "CenterLine=!c,Text=text,Newline=\n");
        ParserTest.assertScans("!c text\n", "CenterLine=!c,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans(" !c text\n", "Whitespace= ,Text=!c,Whitespace= ,Text=text,Newline=\n");
        ParserTest.assertScans("!c text", "CenterLine=!c,Whitespace= ,Text=text");
    }

    @Test public void translatesCenters() {
        ParserTest.assertTranslates("!c some text\n", "<div class=\"centered\">some text</div>" + HtmlElement.endl);
    }

    @Test public void scansNotes() {
        ParserTest.assertScans("!note some note\n", "NoteLine=!note,Whitespace= ,Text=some,Whitespace= ,Text=note,Newline=\n");
        ParserTest.assertScans("! note some note\n", "Text=!,Whitespace= ,Text=note,Whitespace= ,Text=some,Whitespace= ,Text=note,Newline=\n");
    }

    @Test public void translatesNotes() {
        ParserTest.assertTranslates("!note some note\n", "<span class=\"note\">some note</span>" + HtmlElement.endl);
    }
}
