package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.parser.VariableSource;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextMakerTest {
    private SymbolProvider provider = new SymbolProvider();
    private VariableSource source = new TestVariableSource("x", "y");

    @Test
    public void makesText() {
        TokenMatch match = makeMatch("hi");
        assertTrue(match.getToken().isType(SymbolType.Text));
        assertEquals("hi", match.getToken().getContent());
        assertEquals(2, match.getMatchLength());
    }

    private TokenMatch makeMatch(String text) {
        TokenMatch match = new TextMaker(source).make(provider, text);
        return match;
    }

    @Test
    public void makesWikiWord() {
        TokenMatch match = makeMatch("HiMom");
        assertTrue(match.getToken().isType(SymbolType.WikiWord));
        assertEquals("HiMom", match.getToken().getContent());
        assertEquals(5, match.getMatchLength());
    }

    @Test
    public void makesWikiWordWithTrailingText() {
        TokenMatch match = makeMatch("HiMom's");
        assertTrue(match.getToken().isType(SymbolType.WikiWord));
        assertEquals("HiMom", match.getToken().getContent());
        assertEquals(5, match.getMatchLength());
    }

    @Test
    public void makesEMail() {
        TokenMatch match = makeMatch("bob@bl.org");
        assertTrue(match.getToken().isType(SymbolType.EMail));
        assertEquals("bob@bl.org", match.getToken().getContent());
        assertEquals(10, match.getMatchLength());
    }
}
