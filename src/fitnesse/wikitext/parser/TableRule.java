package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule extends Rule {
    private static final SymbolType[]literalTableTypes = {
            SymbolType.EndCell, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
    
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
            return new Parser(getPage()).parse(
                    scanner,
                    new SymbolProvider().setTypes(literalTableTypes),
                    SymbolType.EndCell);
            /*scanner.makeLiteral(SymbolType.EndCell);
            String body = scanner.getCurrentContent();
            scanner.moveNext();
            return new Symbol(SymbolType.Literal, body);*/
        }
        else
            return new Parser(getPage()).parse(scanner, SymbolType.EndCell);
    }
}
