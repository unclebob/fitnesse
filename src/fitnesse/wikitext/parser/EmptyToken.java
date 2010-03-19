package fitnesse.wikitext.parser;

import util.Maybe;

public class EmptyToken implements Token {

    public Maybe<String> render(Scanner scanner) {
        return Maybe.noString;
    }

    public TokenType getType() { return TokenType.Empty; }
}
