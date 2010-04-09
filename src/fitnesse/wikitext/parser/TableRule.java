package fitnesse.wikitext.parser;

import util.Maybe;

public class TableRule extends Rule {
    private static final SymbolType[]literalTableTypes = {
            SymbolType.EndCell, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
    
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
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
            //todo: the text should be Literal
            return Parser.make(getPage(), scanner, new SymbolProvider().setTypes(literalTableTypes), SymbolType.EndCell)
                    .parse();
        }
        else
            return Parser.make(getPage(), scanner, SymbolType.EndCell).parse();
    }
}
