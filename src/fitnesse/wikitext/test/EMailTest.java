package fitnesse.wikitext.test;

import org.junit.Test;

public class EMailTest {
    @Test
    public void parsesEMail() {
        ParserTest.assertParses("bob@bl.org", "SymbolList[EMail]");
    }

    @Test public void translatesEMail() {
        ParserTest.assertTranslatesTo("bob@bl.org", "<a href=\"mailto:bob@bl.org\">bob@bl.org</a>");
    }
}
