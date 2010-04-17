package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.TextMaker;
import fitnesse.wikitext.parser.TokenMatch;
import fitnesse.wikitext.translator.VariableSource;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TextMakerTest {
    private SymbolProvider provider = new SymbolProvider();
    private VariableSource source = new TestVariableSource("x", "y");

    @Test
    public void makesText() {
        TokenMatch match = makeMatch("hi");
        assertEquals(SymbolType.Text, match.getToken().getType());
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
        assertEquals(SymbolType.WikiWord, match.getToken().getType());
        assertEquals("HiMom", match.getToken().getContent());
        assertEquals(5, match.getMatchLength());
    }

    @Test
    public void makesWikiWordWithTrailingText() {
        TokenMatch match = makeMatch("HiMom's");
        assertEquals(SymbolType.WikiWord, match.getToken().getType());
        assertEquals("HiMom", match.getToken().getContent());
        assertEquals(5, match.getMatchLength());
    }

    @Test
    public void makesEMail() {
        TokenMatch match = makeMatch("bob@bl.org");
        assertEquals(SymbolType.EMail, match.getToken().getType());
        assertEquals("bob@bl.org", match.getToken().getContent());
        assertEquals(10, match.getMatchLength());
    }
}
