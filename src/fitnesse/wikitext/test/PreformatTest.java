package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class PreformatTest {
    @Test public void scansPreformats() {
        ParserTest.assertScansTokenType("{{{stuff}}}", "Preformat", true);
    }

    @Test public void translatesPreformats() {
        ParserTest.assertTranslatesTo("{{{stuff}}}", "<pre>stuff</pre>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("{{{''stuff''}}}", "<pre>''stuff''</pre>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("{{{<stuff>}}}", "<pre>&lt;stuff&gt;</pre>" + HtmlElement.endl);
    }
}
