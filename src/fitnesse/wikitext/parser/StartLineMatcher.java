package fitnesse.wikitext.parser;

public class StartLineMatcher implements Matcher {
    private String[] delimiters;
    private Class<? extends Token> tokenClass;

    public StartLineMatcher(String delimiter, Class<? extends Token> tokenClass) {
        this.delimiters = new String[] {delimiter};
        this.tokenClass = tokenClass;
    }

    public StartLineMatcher(String[] delimiters, Class<? extends Token> tokenClass) {
        this.delimiters = delimiters;
        this.tokenClass = tokenClass;
    }

    public TokenMatch makeMatch(TokenType type, ScanString input) {
        if (input.startsLine()) {
            for (String delimiter: delimiters) {
                if (!input.startsWith(delimiter)) continue;

                Token token;
                try {
                    token = tokenClass.newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
                token.setContent(delimiter);
                token.setType(type);
                return new TokenMatch(token, delimiter.length());
            }
        }
        return TokenMatch.noMatch;
    }
}
