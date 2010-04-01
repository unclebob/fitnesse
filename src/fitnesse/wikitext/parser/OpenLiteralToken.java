package fitnesse.wikitext.parser;

import util.Maybe;

public class OpenLiteralToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        scanner.makeLiteral(SymbolType.CloseLiteral);
        if (scanner.isEnd()) return Maybe.noString;
        String literal = scanner.getCurrentContent();
        scanner.moveNext();
        return new Maybe<String>(literal);
    }
}
