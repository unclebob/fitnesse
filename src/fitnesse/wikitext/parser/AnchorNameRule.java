package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AnchorNameRule extends Rule {
    public Maybe<Symbol> parse(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (tokens.size() == 0) return Symbol.Nothing;

        String anchor = tokens.get(1).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.Nothing;

        return new Maybe<Symbol>(new Phrase(SymbolType.AnchorName).add(tokens.get(1)));
    }
}
