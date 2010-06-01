package fitnesse.wikitext.parser;

public class SymbolMatch {
    public static final SymbolMatch noMatch = new SymbolMatch();
    private final Symbol symbol;
    private final int matchLength;

    public SymbolMatch(Symbol token, int matchLength) {
        this.symbol = token;
        this.matchLength = matchLength;
    }
    
    private SymbolMatch() {
        symbol = null;
        matchLength = -1;
    }

    public Symbol getSymbol() { return symbol; }
    public int getMatchLength() { return matchLength; }
    public boolean isMatch() { return symbol != null; }
}
