package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class LineTokenTest {
    @Test public void scansHeaders() {
        ParserTest.assertScans("!1 some text\n", "HeaderLine=1,Whitespace= ,Word=some,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!2 \n", "HeaderLine=2,Whitespace= ,Newline=\n");
        ParserTest.assertScans("!3 text\n", "HeaderLine=3,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!4 text\n", "HeaderLine=4,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!5 text\n", "HeaderLine=5,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!6 text\n", "HeaderLine=6,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!3text\n", "HeaderLine=3,Word=text,Newline=\n");
        ParserTest.assertScans("!0 text\n", "Text=!,Word=0,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!7 text\n", "Text=!,Word=7,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("not start !1 text\n", "Word=not,Whitespace= ,Word=start,Whitespace= ,Text=!,Word=1,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("at start\n!1 text\n", "Word=at,Whitespace= ,Word=start,Newline=\n,HeaderLine=1,Whitespace= ,Word=text,Newline=\n");
    }

    @Test public void translatesHeaders() {
        for (int i = 1; i < 7; i++)
            ParserTest.assertTranslates("!" + i + " some text \n", "<h" + i + ">some text</h" + i + ">" + HtmlElement.endl);
    }

    @Test public void scansCenters() {
        ParserTest.assertScans("!c some text\n", "CenterLine=c,Whitespace= ,Word=some,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!C more text\n", "CenterLine=C,Whitespace= ,Word=more,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!ctext\n", "CenterLine=c,Word=text,Newline=\n");
        ParserTest.assertScans("!c text\n", "CenterLine=c,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans(" !c text\n", "Whitespace= ,Text=!,Word=c,Whitespace= ,Word=text,Newline=\n");
        ParserTest.assertScans("!c text", "CenterLine=c,Whitespace= ,Word=text");
    }

    @Test public void translatesCenters() {
        ParserTest.assertTranslates("!c some text\n", "<div class=\"centered\">some text</div>" + HtmlElement.endl);
    }

    @Test public void scansNotes() {
        ParserTest.assertScans("!note some note\n", "NoteLine=note,Whitespace= ,Word=some,Whitespace= ,Word=note,Newline=\n");
        ParserTest.assertScans("! note some note\n", "Text=!,Whitespace= ,Word=note,Whitespace= ,Word=some,Whitespace= ,Word=note,Newline=\n");
    }

    @Test public void translatesNotes() {
        ParserTest.assertTranslates("!note some note\n", "<span class=\"note\">some note</span>" + HtmlElement.endl);
    }
}
