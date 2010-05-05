package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AnchorNameRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        List<Symbol> tokens = parser.getScanner().nextTokens(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (tokens.size() == 0) return Symbol.nothing;

        String anchor = tokens.get(1).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.AnchorName).add(tokens.get(1)));
    }
}
