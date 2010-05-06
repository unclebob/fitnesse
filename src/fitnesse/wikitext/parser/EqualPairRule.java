package fitnesse.wikitext.parser;

import util.Maybe;

public class EqualPairRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseToIgnoreFirst(current.getType());
        if (parser.atEnd()) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(body));
    }
}
