package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class NewlineTokenTest {
    @Test public void translatesNewlines() {
        ParserTest.assertTranslates("hi\nmom", "hi<br/>" + HtmlElement.endl + "mom");
    }
}
