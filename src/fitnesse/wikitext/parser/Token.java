package fitnesse.wikitext.parser;

import util.Maybe;

public interface Token {
    TokenMatch makeMatch(ScanString input);
    Maybe<String> render(Scanner scanner);
    boolean sameAs(Token other);
}
