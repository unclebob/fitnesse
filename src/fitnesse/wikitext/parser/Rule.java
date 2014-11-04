package fitnesse.wikitext.parser;

import util.Maybe;

public interface Rule {
    Maybe<Symbol> parse(Symbol current, Parser parser);
}
