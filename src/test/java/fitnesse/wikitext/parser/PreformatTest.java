package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class PreformatTest {
    @Test public void scansPreformats() {
        ParserTestHelper.assertScansTokenType("{{{stuff}}}", "Preformat", true);
    }

    @Test public void translatesPreformats() {
        ParserTestHelper.assertTranslatesTo("{{{stuff}}}", "<pre>stuff</pre>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("{{{''stuff''}}}", "<pre>''stuff''</pre>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("{{{<stuff>}}}", "<pre>&lt;stuff&gt;</pre>" + HtmlElement.endl);
    }

    @Test public void translatesVariablesInPreformats() {
        ParserTestHelper.assertTranslatesTo("{{{s${x}f}}}", new TestVariableSource("x", "tuf"), "<pre>stuff</pre>" + HtmlElement.endl);
    }
}
