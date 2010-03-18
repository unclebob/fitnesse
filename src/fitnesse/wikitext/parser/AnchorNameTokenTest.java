package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorNameTokenTest {
    @Test public void scansAnchors() {
        ParserTest.assertScans("!anchor name", "AnchorNameToken=name");
        ParserTest.assertScans("!anchor 1234", "AnchorNameToken=1234");
        ParserTest.assertScans("!anchor @#$@#%", "TextToken=!anchor @#$@#%");
        ParserTest.assertScans("! anchor name", "TextToken=! anchor name");
        ParserTest.assertScans("!anchor name other stuff", "AnchorNameToken=name,TextToken= other stuff");
        ParserTest.assertScans("!anchor name ", "AnchorNameToken=name,TextToken= ");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslates("!anchor name", "<a name=\"name\"> </a>" + HtmlElement.endl);
        ParserTest.assertTranslates("!anchor name stuff", "<a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more!anchor name stuff", "more<a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more !anchor name stuff", "more <a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
    }
}
