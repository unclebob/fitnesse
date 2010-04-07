package fitnesse.wikitext.parser;

import util.Maybe;

public class EvaluatorRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol body = Parser.makeIgnoreFirst(getPage(), scanner, SymbolType.CloseEvaluator).parse();
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Evaluator).add(body));
    }
}
