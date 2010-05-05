package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class DefineRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        List<Symbol> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text, SymbolType.Whitespace});
        if (tokens.size() == 0) return Symbol.nothing;

        String name = tokens.get(1).getContent();
        if (!ScanString.isVariableName(name)) return Symbol.nothing;

        scanner.moveNext();
        SymbolType open = scanner.getCurrent().getType();
        SymbolType close = SymbolType.closeType(open);
        if (close == SymbolType.Empty) return Symbol.nothing;

        int start = scanner.getOffset();
        scanner.markStart();
        Symbol value = parser.parseToIgnoreFirst(close);
        if (scanner.isEnd()) return Symbol.nothing;

        String valueString = scanner.substring(start, scanner.getOffset() - 1);
        parser.getPage().putVariable(name, valueString);

        return new Maybe<Symbol>(new Symbol(SymbolType.Define)
                .add(name)
                .add(value)
                .add(valueString));
    }
}
