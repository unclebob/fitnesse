package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule extends Rule {

    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol table = scanner.getCurrent();
        String content = table.getContent();
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            table.add(row);
            while (true) {
                Symbol cell = parseCell(scanner, content);
                if (scanner.isEnd()) return Symbol.Nothing;
                row.add(cell);
                if (scanner.getCurrentContent().indexOf("\n") > 0) break;
            }
            if (scanner.getCurrentContent().indexOf("\n|") < 0) break;
        }
        return new Maybe<Symbol>(table);
    }

    private Symbol parseCell(Scanner scanner, String content) {
        if (content.indexOf("!") >= 0) {
            return Parser.make(getPage(), scanner, new SymbolProvider().setTypes(SymbolProvider.literalTableTypes), SymbolType.EndCell)
                    .parse();
        }
        else
            return Parser.make(getPage(), scanner, SymbolType.EndCell).parse();
    }
}
