package fitnesse.wikitext.parser;

import org.junit.Test;

public class NewlineTest {
    @Test public void translatesNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\nmom", "hi" + ParserTestHelper.newLineRendered + "mom");
    }
}
