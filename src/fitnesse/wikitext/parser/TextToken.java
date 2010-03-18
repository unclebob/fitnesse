package fitnesse.wikitext.parser;

import util.Maybe;

public class TextToken extends ContentToken {
    public TextToken(String content) { super(content); }

    public TokenMatch makeMatch(String input, int offset) {
        return TokenMatch.noMatch;
    }

    public boolean sameAs(Token other) { return false; }
}

