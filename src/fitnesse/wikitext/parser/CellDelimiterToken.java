package fitnesse.wikitext.parser;

public class CellDelimiterToken extends ContentToken {
    public CellDelimiterToken(String content) { super(content); }
    public CellDelimiterToken() { this(""); }

    public TokenMatch makeMatch(ScanString input) {
        return input.startsWith("|\n|") ? new TokenMatch(new CellDelimiterToken("|\n|"), 3)
                : input.startsWith("|\n") ? new TokenMatch(new CellDelimiterToken("|\n"), 2)
                : input.startsWith("|") ? new TokenMatch(new CellDelimiterToken("|"), 1)
                : TokenMatch.noMatch;
    }

    public boolean sameAs(Token other) {
        return other instanceof CellDelimiterToken;
    }
}
