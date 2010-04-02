package fitnesse.wikitext.parser;

import util.Maybe;

public class EvaluatorRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol body = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.CloseEvaluator);
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Evaluator).add(body));
    }
}
