package fitnesse.wikitext.parser;

import util.Maybe;

public class EndSectionToken implements Token {

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>("");
    }

    public TokenType getType() { return TokenType.EndSection; }
}
