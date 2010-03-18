package fitnesse.wikitext.parser;

import util.Maybe;

public class EmptyToken implements Token {

    public TokenMatch makeMatch(ScanString input) {
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        return Maybe.noString;
    }

    public boolean sameAs(Token other) { return false; }
}
