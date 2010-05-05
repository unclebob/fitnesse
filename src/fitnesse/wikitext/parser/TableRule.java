package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol table = parser.getCurrent();
        String content = table.getContent();
        if (table.getContent().charAt(0) == '-') table.putProperty("hideFirst", "");
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            table.add(row);
            while (true) {
                Symbol cell = parseCell(parser, content);
                if (scanner.isEnd()) return Symbol.nothing;
                row.add(cell);
                if (scanner.getCurrentContent().indexOf("\n") > 0 || scanner.isLast()) break;
            }
            if (scanner.getCurrentContent().indexOf("\n|") < 0 || scanner.isLast()) break;
        }
        return new Maybe<Symbol>(table);
    }

    private Symbol parseCell(Parser parser, String content) {
        if (content.indexOf("!") >= 0) {
            return parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.literalTableTypes);
        }
        else
            return parser.parseTo(SymbolType.EndCell);
    }
}
