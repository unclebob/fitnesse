package fitnesse.wikitext.parser;

import util.Maybe;

public class PlainTextTableRule implements Rule {
    private static final SymbolType[] terminators = new SymbolType[]
            {SymbolType.PlainTextCellSeparator, SymbolType.Newline, SymbolType.ClosePlainTextTable};

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol table = parser.getCurrent();
        table.putProperty("class", "plain_text_table");

        parser.moveNext(1);
        if (parser.atEnd()) return Symbol.nothing;

        Matchable[] plainTextTableTypes;
        if (!parser.getCurrent().isType(SymbolType.Newline) && !parser.getCurrent().isType(SymbolType.Whitespace)) {
            Matchable columnSeparator = new ColumnSeparator(parser.getCurrent().getContent().substring(0, 1));
            plainTextTableTypes = new Matchable[]
                {columnSeparator, SymbolType.Newline, SymbolType.ClosePlainTextTable, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
            parser.moveNext(1);
            if (parser.atEnd()) return Symbol.nothing;
        }
        else {
            plainTextTableTypes = new Matchable[]
                {SymbolType.Newline, SymbolType.ClosePlainTextTable, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
        }

        if (parser.getCurrent().isType(SymbolType.Whitespace)) {
            table.putProperty("hideFirst", "");
        }
        
        Symbol row = null;
        while (true) {
            Symbol line = parser.parseToWithSymbols(terminators, new SymbolProvider(plainTextTableTypes));
            if (parser.atEnd()) return Symbol.nothing;
            if (parser.getCurrent().isType(SymbolType.ClosePlainTextTable)) return new Maybe<Symbol>(table);
            if (row == null) {
                row = new Symbol(SymbolType.SymbolList);
                table.add(row);
            }
            row.add(line);
            if (parser.getCurrent().isType(SymbolType.Newline)) row = null;
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
