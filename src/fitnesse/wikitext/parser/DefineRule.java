package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class DefineRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> tokens = parser.moveNext(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text, SymbolType.Whitespace});
        if (tokens.size() == 0) return Symbol.nothing;

        String name = tokens.get(1).getContent();
        if (!ScanString.isVariableName(name)) return Symbol.nothing;

        Symbol next = parser.moveNext(1);
        SymbolType open = next.getType();
        SymbolType close = open.closeType();
        if (close == SymbolType.Empty) return Symbol.nothing;

        String valueString = parser.parseToIgnoreFirstAsString(close);
        if (parser.atEnd()) return Symbol.nothing;
        parser.getPage().putVariable(name, valueString);

        return new Maybe<Symbol>(new Symbol(SymbolType.Define)
                .add(name)
                .add(valueString));
    }
}
