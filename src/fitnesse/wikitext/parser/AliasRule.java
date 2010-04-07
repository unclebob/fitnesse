package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AliasRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol tag = Parser.makeIgnoreFirst(getPage(), scanner,SymbolType.CloseBracket).parse();
        if (scanner.isEnd()) return Symbol.Nothing;

        scanner.moveNext();
        if (!scanner.isType(SymbolType.OpenBracket)) return Symbol.Nothing;

        Symbol link = Parser.makeIgnoreFirst(getPage(), scanner, SymbolType.CloseBracket).parse();

        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.CloseBracket});
        if (tokens.size() == 0) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Alias)
                .add(tag)
                .add(link));
    }
}
