package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class LineTokenTest {
    @Test public void scansHeaders() {
        ParserTest.assertScans("!1 some text\n", "LineToken=1,TextToken=some text,NewlineToken=\n");
        ParserTest.assertScans("!2 \n", "LineToken=2,NewlineToken=\n");
        ParserTest.assertScans("!3 text\n", "LineToken=3,TextToken=text,NewlineToken=\n");
        ParserTest.assertScans("!4 text\n", "LineToken=4,TextToken=text,NewlineToken=\n");
        ParserTest.assertScans("!5 text\n", "LineToken=5,TextToken=text,NewlineToken=\n");
        ParserTest.assertScans("!6 text\n", "LineToken=6,TextToken=text,NewlineToken=\n");
        ParserTest.assertScans("!3text\n", "TextToken=!3text,NewlineToken=\n");
        ParserTest.assertScans("!0 text\n", "TextToken=!0 text,NewlineToken=\n");
        ParserTest.assertScans("!7 text\n", "TextToken=!7 text,NewlineToken=\n");
        ParserTest.assertScans("not start !1 text\n", "TextToken=not start !1 text,NewlineToken=\n");
        ParserTest.assertScans("at start\n!1 text\n", "TextToken=at start,NewlineToken=\n,LineToken=1,TextToken=text,NewlineToken=\n");
    }

    @Test public void translatesHeaders() {
        for (int i = 1; i < 7; i++)
            ParserTest.assertTranslates("!" + i + " some text \n", "<h" + i + ">some text</h" + i + ">" + HtmlElement.endl);
    }

    @Test public void scansCenters() {
        ParserTest.assertScans("!c some text\n", "LineToken=c,TextToken=some text,NewlineToken=\n");
        ParserTest.assertScans("!C more text\n", "LineToken=C,TextToken=more text,NewlineToken=\n");
        ParserTest.assertScans("!ctext\n", "TextToken=!ctext,NewlineToken=\n");
        ParserTest.assertScans("!c text\n", "LineToken=c,TextToken=text,NewlineToken=\n");
        ParserTest.assertScans(" !c text\n", "TextToken= !c text,NewlineToken=\n");
        ParserTest.assertScans("!c text", "LineToken=c,TextToken=text");
    }

    @Test public void translatesCenters() {
        ParserTest.assertTranslates("!c some text\n", "<div class=\"centered\">some text</div>" + HtmlElement.endl);
    }

    @Test public void scansNotes() {
        ParserTest.assertScans("!note some note\n", "LineToken=note,TextToken=some note,NewlineToken=\n");
        ParserTest.assertScans("! note some note\n", "TextToken=! note some note,NewlineToken=\n");
    }

    @Test public void translatesNotess() {
        ParserTest.assertTranslates("!note some note\n", "<span class=\"note\">some note</span>" + HtmlElement.endl);
    }
}
