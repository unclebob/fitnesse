package fitnesse.wikitext.parser;

import util.Maybe;

public class PlainTextTable extends SymbolType implements Rule {
    public PlainTextTable() {
        super("PlainTextTable");
        wikiMatcher(new Matcher().startLine().string("!["));
        wikiRule(this);
        htmlTranslation(new Table());
    }
    
    private static final SymbolType[] terminators = new SymbolType[]
            {SymbolType.PlainTextCellSeparator, SymbolType.Newline, SymbolType.ClosePlainTextTable};

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol table = parser.getCurrent();
        table.putProperty("class", "plain_text_table");

        parser.moveNext(1);
        if (parser.atEnd()) return Symbol.nothing;

        SymbolProvider plainTextTableTypes;
        if (!parser.getCurrent().isType(SymbolType.Newline) && !parser.getCurrent().isType(SymbolType.Whitespace)) {
            Matchable columnSeparator = new ColumnSeparator(parser.getCurrent().getContent().substring(0, 1));
            plainTextTableTypes = new SymbolProvider(new SymbolType[]
                {SymbolType.Newline, SymbolType.ClosePlainTextTable, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});
            plainTextTableTypes.addMatcher(columnSeparator);
            parser.moveNext(1);
            if (parser.atEnd()) return Symbol.nothing;
        }
        else {
            plainTextTableTypes = new SymbolProvider(new SymbolType[]
                {SymbolType.Newline, SymbolType.ClosePlainTextTable, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});
        }

        if (parser.getCurrent().isType(SymbolType.Whitespace)) {
            table.putProperty("hideFirst", "");
        }
        
        Symbol row = null;
        while (true) {
            Symbol line = parser.parseToWithSymbols(terminators, plainTextTableTypes, 0);
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

        public boolean matchesFor(SymbolType symbolType) {
            return symbolType == SymbolType.PlainTextCellSeparator;
        }

        public SymbolMatch makeMatch(ScanString input) {
            Maybe<Integer> matchLength = matcher.makeMatch(input);
            return matchLength.isNothing()
                    ? SymbolMatch.noMatch
                    : new SymbolMatch(SymbolType.PlainTextCellSeparator, input, matchLength.getValue());
        }
    }
}
