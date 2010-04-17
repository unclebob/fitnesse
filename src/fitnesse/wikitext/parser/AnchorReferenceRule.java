package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AnchorReferenceRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        List<Symbol> tokens = parser.getScanner().nextTokens(new SymbolType[] {SymbolType.Text});
        if (tokens.size() == 0) return Symbol.Nothing;

        String anchor = tokens.get(0).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.AnchorReference).add(tokens.get(0)));
    }
}
