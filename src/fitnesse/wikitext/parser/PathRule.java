package fitnesse.wikitext.parser;

import util.Maybe;

public class PathRule implements Rule {

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(parser.parseWithEnds(SymbolProvider.pathRuleProvider, new SymbolType[] {SymbolType.Newline})));
    }
}
