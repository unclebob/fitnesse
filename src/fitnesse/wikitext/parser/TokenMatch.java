package fitnesse.wikitext.parser;

public class TokenMatch {
    public static final TokenMatch noMatch = new TokenMatch();
    private final Symbol token;
    private final int matchLength;

    public TokenMatch(Symbol token, int matchLength) {
        this.token = token;
        this.matchLength = matchLength;
    }
    
    private TokenMatch() {
        token = null;
        matchLength = -1;
    }

    public Symbol getToken() { return token; }
    public int getMatchLength() { return matchLength; }
    public boolean isMatch() { return token != null; }
}
