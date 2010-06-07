package fitnesse.wikitext.parser;

import util.Maybe;

public class AliasRule implements Rule {

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol tag = parser.parseToIgnoreFirst(SymbolType.CloseBracket);
        if (!parser.isMoveNext(SymbolType.OpenBracket)) return Symbol.nothing;

        Symbol link = parser.parseToIgnoreFirstWithSymbols(SymbolType.CloseBracket, SymbolProvider.aliasLinkProvider);
        if (!parser.isMoveNext(SymbolType.CloseBracket)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(tag).add(link));
    }
}
