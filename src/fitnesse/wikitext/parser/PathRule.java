package fitnesse.wikitext.parser;

import util.Maybe;

public class PathRule implements Rule {
    private static final SymbolProvider provider = new SymbolProvider(new SymbolType[] {
          SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable
    });
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(parser.parseWithEnds(provider, new SymbolType[] {SymbolType.Newline})));
    }
}
