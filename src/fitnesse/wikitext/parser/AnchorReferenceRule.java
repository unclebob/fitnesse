package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AnchorReferenceRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> tokens = parser.moveNext(new SymbolType[] {SymbolType.Text});
        if (tokens.size() == 0) return Symbol.nothing;

        String anchor = tokens.get(0).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(tokens.get(0)));
    }
}
