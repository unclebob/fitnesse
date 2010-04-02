package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        String content = scanner.getCurrentContent();
        Symbol table = new Symbol(SymbolType.Table);
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
        if (content.startsWith("!")) {
            scanner.makeLiteral(SymbolType.EndCell);
            String body = scanner.getCurrentContent();
            scanner.moveNext();
            return new Symbol(SymbolType.Literal, body);
        }
        return new Parser(getPage()).parse(scanner, SymbolType.EndCell);
    }
}
