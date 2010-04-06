package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.TextMaker;
import fitnesse.wikitext.parser.TokenMatch;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TextMakerTest {
    private SymbolProvider provider = new SymbolProvider();

    @Test
    public void makesText() {
        TokenMatch match = new TextMaker().make(provider, "hi");
        assertEquals(SymbolType.Text, match.getToken().getType());
        assertEquals("hi", match.getToken().getContent());
        assertEquals(2, match.getMatchLength());
    }

    @Test
    public void makesWikiWord() {
        TokenMatch match = new TextMaker().make(provider, "HiMom");
        assertEquals(SymbolType.WikiWord, match.getToken().getType());
        assertEquals("HiMom", match.getToken().getContent());
        assertEquals(5, match.getMatchLength());
    }

    @Test
    public void makesWikiWordWithTrailingText() {
        TokenMatch match = new TextMaker().make(provider, "HiMom's");
        assertEquals(SymbolType.WikiWord, match.getToken().getType());
        assertEquals("HiMom", match.getToken().getContent());
        assertEquals(5, match.getMatchLength());
    }
}
