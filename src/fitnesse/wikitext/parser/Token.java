package fitnesse.wikitext.parser;

import util.Maybe;

public interface Token {
    Maybe<String> render(Scanner scanner);
    TokenType getType();
}
