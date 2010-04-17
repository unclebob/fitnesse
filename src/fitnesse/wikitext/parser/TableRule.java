package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol table = scanner.getCurrent();
        String content = table.getContent();
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            table.add(row);
            while (true) {
                Symbol cell = parseCell(parser, content);
                if (scanner.isEnd()) return Symbol.Nothing;
                row.add(cell);
                if (scanner.getCurrentContent().indexOf("\n") > 0) break;
            }
            if (scanner.getCurrentContent().indexOf("\n|") < 0) break;
        }
        return new Maybe<Symbol>(table);
    }

    private Symbol parseCell(Parser parser, String content) {
        if (content.indexOf("!") >= 0) {
            return Parser.make(parser.getPage(), parser.getScanner(), new SymbolProvider().setTypes(SymbolProvider.literalTableTypes), SymbolType.EndCell)
                    .parse();
        }
        else
            return parser.parseTo(SymbolType.EndCell);
    }
}
