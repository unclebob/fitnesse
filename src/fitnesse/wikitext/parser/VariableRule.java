package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class VariableRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Text, SymbolType.CloseBrace});
        if (tokens.size() == 0) return Symbol.Nothing;

        String name = tokens.get(0).getContent();
        if (!ScanString.isWord(name)) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Variable).add(name));
    }
}
