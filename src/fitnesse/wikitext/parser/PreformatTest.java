package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class PreformatTest {
    @Test public void scansPreformats() {
        ParserTest.assertScansTokenType("{{{stuff}}}", SymbolType.Preformat, true);
    }

    @Test public void translatesPreformats() {
        ParserTest.assertTranslatesTo("{{{stuff}}}", "<pre>stuff</pre>" + HtmlElement.endl);
    }
}
