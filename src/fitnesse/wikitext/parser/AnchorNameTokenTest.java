package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorNameTokenTest {
    @Test public void scansAnchors() {
        ParserTest.assertScansTokenType("!anchor name", TokenType.AnchorName, true);
        ParserTest.assertScansTokenType("!anchor 1234", TokenType.AnchorName, true);
        ParserTest.assertScansTokenType("!anchor @#$@#%", TokenType.AnchorName, true);
        ParserTest.assertScansTokenType("! anchor name", TokenType.AnchorName, false);
        ParserTest.assertScansTokenType("!anchor name other stuff", TokenType.AnchorName, true);
        ParserTest.assertScansTokenType("!anchor name ", TokenType.AnchorName, true);
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslates("!anchor name", "<a name=\"name\"> </a>" + HtmlElement.endl);
        ParserTest.assertTranslates("!anchorname", "!anchorname");
        ParserTest.assertTranslates("!anchor name stuff", "<a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more!anchor name stuff", "more<a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more !anchor name stuff", "more <a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
    }
}
