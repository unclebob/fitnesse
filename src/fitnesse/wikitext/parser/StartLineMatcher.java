package fitnesse.wikitext.parser;

public class StartLineMatcher implements Matcher {
    private String delimiter;
    private Class<? extends Token> tokenClass;

    public StartLineMatcher(String delimiter, Class<? extends Token> tokenClass) {
        this.delimiter = delimiter;
        this.tokenClass = tokenClass;
    }
    
    public TokenMatch makeMatch(TokenType type, ScanString input) {
        if (input.startsLine() & input.startsWith(delimiter)) {
            Token token;
            try {
                token = tokenClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
            token.setType(type);
            return new TokenMatch(token, delimiter.length());
        }
        return TokenMatch.noMatch;
    }
}
