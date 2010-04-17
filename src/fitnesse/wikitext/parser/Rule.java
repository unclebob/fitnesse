package fitnesse.wikitext.parser;

import util.Maybe;

public interface Rule {
    Maybe<Symbol> parse(Parser parser);
}
