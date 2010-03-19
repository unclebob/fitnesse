package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorNameTokenTest {
    @Test public void scansAnchors() {
        ParserTest.assertScans("!anchor name", "AnchorName,Whitespace= ,Word=name");
        ParserTest.assertScans("!anchor 1234", "AnchorName,Whitespace= ,Word=1234");
        ParserTest.assertScans("!anchor @#$@#%", "AnchorName,Whitespace= ,Text=@#$@#%");
        ParserTest.assertScans("! anchor name", "Text=!,Whitespace= ,Word=anchor,Whitespace= ,Word=name");
        ParserTest.assertScans("!anchor name other stuff", "AnchorName,Whitespace= ,Word=name,Whitespace= ,Word=other,Whitespace= ,Word=stuff");
        ParserTest.assertScans("!anchor name ", "AnchorName,Whitespace= ,Word=name,Whitespace= ");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslates("!anchor name", "<a name=\"name\"> </a>" + HtmlElement.endl);
        ParserTest.assertTranslates("!anchorname", "!anchorname");
        ParserTest.assertTranslates("!anchor name stuff", "<a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more!anchor name stuff", "more<a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more !anchor name stuff", "more <a name=\"name\"> </a>" + HtmlElement.endl + " stuff");
    }
}
