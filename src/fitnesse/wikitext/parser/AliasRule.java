package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AliasRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol tag = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.CloseBracket);
        if (scanner.isEnd()) return Symbol.Nothing;

        scanner.moveNext();
        if (!scanner.isType(SymbolType.OpenBracket)) return Symbol.Nothing;

        scanner.moveNext();
        if (!scanner.isType(SymbolType.Text) && !scanner.isType(SymbolType.WikiWord)) return Symbol.Nothing;
        Symbol link = scanner.getCurrent();

        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.CloseBracket, SymbolType.CloseBracket});
        if (tokens.size() == 0) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Alias)
                .add(tag)
                .add(link));
    }
}
