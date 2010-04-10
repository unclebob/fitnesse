package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import org.junit.Test;

public class NewlineTest {
    @Test public void translatesNewlines() {
        ParserTest.assertTranslatesTo("hi\nmom", "hi<br/>mom");
    }
}
