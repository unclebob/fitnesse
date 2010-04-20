package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class AliasRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol tag = parser.parseIgnoreFirst(SymbolType.CloseBracket);
        if (scanner.isEnd()) return Symbol.Nothing;

        scanner.moveNext();
        if (!scanner.isType(SymbolType.OpenBracket)) return Symbol.Nothing;

        Symbol link = parser.parseIgnoreFirstWithSymbols(SymbolType.CloseBracket, SymbolProvider.aliasLinkTypes);

        List<Symbol> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.CloseBracket});
        if (tokens.size() == 0) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Alias)
                .add(tag)
                .add(link));
    }
}
