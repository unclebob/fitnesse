package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule implements Rule {
    private static final SymbolProvider literalTableProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.EndCell, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable});

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String content = current.getContent();
        if (content.charAt(0) == '-') current.putProperty("hideFirst", "");
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            current.add(row);
            while (true) {
                Symbol cell = parseCell(parser, content);
                if (parser.atEnd()) return Symbol.nothing;
                row.add(cell);
                if (parser.getCurrent().getContent().indexOf("\n") > 0 || parser.atLast()) break;
            }
            if (parser.getCurrent().getContent().indexOf("\n|") < 0 || parser.atLast()) break;
        }
        return new Maybe<Symbol>(current);
    }

    private Symbol parseCell(Parser parser, String content) {
        if (content.indexOf("!") >= 0) {
            return parser.parseToWithSymbols(SymbolType.EndCell, literalTableProvider);
        }
        else
            return parser.parseTo(SymbolType.EndCell);
    }
}
