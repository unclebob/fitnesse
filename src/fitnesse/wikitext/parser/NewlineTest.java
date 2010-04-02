package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class NewlineTest {
    @Test public void translatesNewlines() {
        ParserTest.assertTranslatesTo("hi\nmom", "hi<br/>" + HtmlElement.endl + "mom");
    }
}
