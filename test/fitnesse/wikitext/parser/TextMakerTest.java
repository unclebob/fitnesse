package fitnesse.wikitext.parser;

import fitnesse.wikitext.SourcePage;
import fitnesse.wikitext.VariableSource;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextMakerTest {
    private VariableSource source = new TestVariableSource("x", "y");
    private SourcePage sourcePage = new TestSourcePage();

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
        SymbolMatch match = makeMatch(input);
        assertTrue(match.getSymbol().isType(SymbolType.Text));
        assertEquals(input, match.getSymbol().getContent());
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
        SymbolMatch match = makeMatch(input);
        assertTrue(match.getSymbol().isType(WikiWord.symbolType));
        assertEquals(wikiWord, match.getSymbol().getContent());
        assertEquals(wikiWord.length(), match.getMatchLength());
    }

    @Test
    public void makesEMail() {
        SymbolMatch match = makeMatch("bob@bl.org");
        assertTrue(match.getSymbol().isType(SymbolType.EMail));
        assertEquals("bob@bl.org", match.getSymbol().getContent());
        assertEquals(10, match.getMatchLength());
    }

    private SymbolMatch makeMatch(String text) {
        return new TextMaker(source, sourcePage).make(new ParseSpecification(), 0, text);
    }
}
