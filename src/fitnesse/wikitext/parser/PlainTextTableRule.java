package fitnesse.wikitext.parser;

import util.Maybe;

public class PlainTextTableRule implements Rule {
    private static final SymbolType[] terminators = new SymbolType[] {SymbolType.Newline, SymbolType.ClosePlainTextTable};
    public Maybe<Symbol> parse(Parser parser) {
        Symbol current = parser.getCurrent();
        current.putProperty("class", "plain_text_table");
        Symbol firstLine = parser.parseTo(terminators);
        if (parser.getScanner().isEnd()) return Symbol.Nothing;
        while (true) {
            Symbol line = parser.parseTo(terminators);
            if (parser.getScanner().isEnd()) return Symbol.Nothing;
            if (parser.getCurrent().getType() == SymbolType.ClosePlainTextTable) break;
            Symbol row = new Symbol(SymbolType.SymbolList);
            current.add(row.add(line));
        }
        return new Maybe<Symbol>(current);
    }
}
