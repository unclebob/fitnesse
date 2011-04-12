package fitnesse.wikitext.parser;

import util.Maybe;

public class EqualPairRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseToIgnoreFirst(current.getType());
        if (body.getChildren().size() == 0)  return Symbol.nothing;
        if (!parser.getCurrent().isType(current.getType())) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(body));
    }
}
