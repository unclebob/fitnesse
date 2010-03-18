package fitnesse.wikitext.parser;

public class TokenMatch {
    public static final TokenMatch noMatch = new TokenMatch();
    private final Token token;
    private final int matchLength;

    public TokenMatch(Token token, int matchLength) {
        this.token = token;
        this.matchLength = matchLength;
    }
    
    private TokenMatch() {
        token = null;
        matchLength = -1;
    }

    public Token getToken() { return token; }
    public int getMatchLength() { return matchLength; }
    public boolean isMatch() { return token != null; }
}
