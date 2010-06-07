package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule implements Rule {

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
            return parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.literalTableProvider);
        }
        else
            return parser.parseTo(SymbolType.EndCell);
    }
}
