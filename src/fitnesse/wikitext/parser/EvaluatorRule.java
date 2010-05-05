package fitnesse.wikitext.parser;

import util.Maybe;

public class EvaluatorRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol body = parser.parseTo(SymbolType.CloseEvaluator);
        if (scanner.isEnd()) return Symbol.nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Evaluator).add(body));
    }
}
