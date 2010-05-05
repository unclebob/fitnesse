package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class SeeRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Symbol current = parser.getScanner().getCurrent();
        List<Symbol> tokens = parser.getScanner().nextTokens(new SymbolType[] {SymbolType.WikiWord});
        if (tokens.size() == 0) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(tokens.get(0)));
    }
}
