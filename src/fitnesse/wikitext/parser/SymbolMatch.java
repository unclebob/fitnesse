package fitnesse.wikitext.parser;

public class SymbolMatch {
    public static final SymbolMatch noMatch = new SymbolMatch();
    private final Symbol symbol;
    private final int offset;
    private final int matchLength;

    public SymbolMatch(Symbol token, int offset, int matchLength) {
        this.symbol = token;
        this.offset = offset;
        this.matchLength = matchLength;
    }

    public SymbolMatch(SymbolType symbolType, ScanString input, MatchResult match) {
      offset = input.getOffset();
      matchLength = match.getLength();
      symbol = new Symbol(symbolType, input.substring(0, matchLength), offset);
      for (String option: match.getOptions()) symbol.putProperty(option, "");
    }

    public SymbolMatch(SymbolType symbolType, String text, int offset) {
        this.symbol = new Symbol(symbolType, text, offset);
        this.offset = offset;
        this.matchLength = text.length();
    }

    private SymbolMatch() {
        symbol = null;
        offset = -1;
        matchLength = -1;
    }

    public Symbol getSymbol() { return symbol; }
    public int getOffset() { return offset; }
    public int getMatchLength() { return matchLength; }
    public boolean isMatch() { return symbol != null; }
}
