package fitnesse.wikitext.test;

import fitnesse.wiki.PathParser;
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
        assertText("hi");
    }

    @Test
    public void makesTextFromWikiWordWithUnderscore() {
        // this replicates old parser behavior based on bug (IMO) in WikiWord regexp
        assertText("HiMom_Dad");
    }

    private void assertText(String input) {
        TokenMatch match = makeMatch(input);
        assertTrue(match.getToken().isType(SymbolType.Text));
        assertEquals(input, match.getToken().getContent());
        assertEquals(input.length(), match.getMatchLength());
    }

    @Test
    public void makesWikiWord() {
        assertWikiWord("HiMom", "HiMom");
    }

    @Test
    public void makesWikiWordWithTrailingText() {
        assertWikiWord("HiMom's", "HiMom");
    }

    @Test
    public void makesWikiWordWithIncludedDots() {
        assertWikiWord("HiMom.HiDad", "HiMom.HiDad");
    }

    @Test
    public void makesWikiWordWithTrailingDots() {
        assertWikiWord("HiMom..HiDad", "HiMom");
    }

    private void assertWikiWord(String input, String wikiWord) {
        TokenMatch match = makeMatch(input);
        assertTrue(match.getToken().isType(SymbolType.WikiWord));
        assertEquals(wikiWord, match.getToken().getContent());
        assertEquals(wikiWord.length(), match.getMatchLength());
    }

    @Test
    public void makesEMail() {
        TokenMatch match = makeMatch("bob@bl.org");
        assertTrue(match.getToken().isType(SymbolType.EMail));
        assertEquals("bob@bl.org", match.getToken().getContent());
        assertEquals(10, match.getMatchLength());
    }

    private TokenMatch makeMatch(String text) {
        return new TextMaker(source).make(provider, text);
    }
}
