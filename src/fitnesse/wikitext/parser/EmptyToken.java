package fitnesse.wikitext.parser;

import util.Maybe;

public class EmptyToken extends Token {
    public Maybe<String> render(Scanner scanner) { return Maybe.noString; }
    public SymbolType getType() { return SymbolType.Empty; }
}
