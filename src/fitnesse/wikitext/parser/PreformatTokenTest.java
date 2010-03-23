package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class PreformatTokenTest {
    @Test public void scansPreformats() {
        ParserTest.assertScansTokenType("{{{stuff}}}", TokenType.Preformat, true);
    }

    @Test public void translatesPreformats() {
        ParserTest.assertTranslates("{{{stuff}}}", "<pre>stuff</pre>" + HtmlElement.endl);
    }
}
