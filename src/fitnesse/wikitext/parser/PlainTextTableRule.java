package fitnesse.wikitext.parser;

import util.Maybe;

public class PlainTextTableRule implements Rule {
    private static final SymbolType[] terminators = new SymbolType[]
            {SymbolType.PlainTextCellSeparator, SymbolType.Newline, SymbolType.ClosePlainTextTable};

    public Maybe<Symbol> parse(Parser parser) {
        Symbol table = parser.getCurrent();
        table.putProperty("class", "plain_text_table");

        parser.moveNext(1);
        if (parser.getScanner().isEnd()) return Symbol.Nothing;

        Matchable[] plainTextTableTypes;
        if (parser.getCurrent().getType() != SymbolType.Newline && parser.getCurrent().getType() != SymbolType.Whitespace) {
            Matchable columnSeparator = new ColumnSeparator(parser.getCurrent().getContent().substring(0, 1));
            plainTextTableTypes = new Matchable[]
                {columnSeparator, SymbolType.Newline, SymbolType.ClosePlainTextTable, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
            parser.moveNext(1);
            if (parser.getScanner().isEnd()) return Symbol.Nothing;
        }
        else {
            plainTextTableTypes = new Matchable[]
                {SymbolType.Newline, SymbolType.ClosePlainTextTable, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
        }

        if (parser.getCurrent().getType() == SymbolType.Whitespace) {
            table.putProperty("hideFirst", "");
        }
        
        Symbol row = null;
        while (true) {
            Symbol line = parser.parseToWithSymbols(terminators, plainTextTableTypes);
            if (parser.getScanner().isEnd()) return Symbol.Nothing;
            if (parser.getCurrent().getType() == SymbolType.ClosePlainTextTable) return new Maybe<Symbol>(table);
            if (row == null) {
                row = new Symbol(SymbolType.SymbolList);
                table.add(row);
            }
            row.add(line);
            if (parser.getCurrent().getType() == SymbolType.Newline) row = null;
        }
    }
    
    private class ColumnSeparator implements Matchable {
        private Matcher matcher;

        public ColumnSeparator(String separator) {
            matcher = new Matcher().string(separator);
        }

        public TokenMatch makeMatch(ScanString input) {
            return matcher.makeMatch(SymbolType.PlainTextCellSeparator, input);
        }
    }
}
