package fitnesse.wikitext.parser;

import util.Maybe;

public class EvaluatorRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseTo(SymbolType.CloseEvaluator);
        if (parser.atEnd()) return Symbol.nothing;
        return new Maybe<Symbol>(current.add(body));
    }
}
