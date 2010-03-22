package fitnesse.wikitext.parser;

import util.Maybe;

public class OpenLiteralToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        scanner.makeLiteral(TokenType.CloseLiteral);
        if (scanner.isEnd()) return Maybe.noString;
        return new Maybe<String>(scanner.getCurrent().getContent());
    }
}
