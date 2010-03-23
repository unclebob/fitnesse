package fitnesse.wikitext.parser;

import util.Maybe;

public class OpenLiteralToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        scanner.makeLiteral(TokenType.CloseLiteral);
        if (scanner.isEnd()) return Maybe.noString;
        String literal = scanner.getCurrent().getContent();
        scanner.moveNext();
        return new Maybe<String>(literal);
    }
}
