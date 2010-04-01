package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class AnchorReferenceRule extends Rule {

    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Text});
        if (tokens.size() == 0) return Symbol.Nothing;

        String anchor = tokens.get(0).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.Nothing;

        return new Maybe<Symbol>(new Phrase(SymbolType.AnchorReference).add(tokens.get(0)));
    }
}
