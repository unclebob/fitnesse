package fitnesse.wikitext.parser;

public class PlainTextTable extends SymbolType implements Rule {
    public PlainTextTable() {
        super("PlainTextTable");
        wikiMatcher(new Matcher().startLine().string("!["));
        wikiRule(this);
        htmlTranslation(new Table());
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol table = parser.getCurrent();
        table.putProperty("class", "plain_text_table");

        parser.moveNext(1);
        if (parser.atEnd()) return Symbol.nothing;

        SymbolProvider plainTextTableTypes;
        SymbolType[] terminators;
        Symbol cellSeparator = parser.getCurrent();
        if (!cellSeparator.isType(SymbolType.Newline) && !cellSeparator.isType(SymbolType.Whitespace)) {
            SymbolType plainTextCellSeparator = new SymbolType("PlainTextCellSeparator");
            plainTextCellSeparator.wikiMatcher(new Matcher().string(cellSeparator.getContent().substring(0, 1)));
            plainTextTableTypes = new SymbolProvider(new SymbolType[]
                {SymbolType.Newline, SymbolType.ClosePlainTextTable, Evaluator.symbolType, Literal.symbolType, Variable.symbolType, plainTextCellSeparator});
            terminators = new SymbolType[] {plainTextCellSeparator, SymbolType.Newline, SymbolType.ClosePlainTextTable};
            parser.moveNext(1);
            if (parser.atEnd()) return Symbol.nothing;
        }
        else {
            plainTextTableTypes = new SymbolProvider(new SymbolType[]
                {SymbolType.Newline, SymbolType.ClosePlainTextTable, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});
            terminators = new SymbolType[] {SymbolType.Newline, SymbolType.ClosePlainTextTable};
        }

        if (parser.getCurrent().isType(SymbolType.Whitespace)) {
            table.putProperty("hideFirst", "");
        }

        Symbol row = null;
        while (true) {
            Symbol line = parser.parseToWithSymbols(terminators, plainTextTableTypes, 0);
            if (parser.atEnd()) return Symbol.nothing;
            if (parser.getCurrent().isType(SymbolType.ClosePlainTextTable)) return new Maybe<>(table);
            if (row == null) {
                row = new Symbol(SymbolType.SymbolList);
                table.add(row);
            }
            row.add(line);
            if (parser.getCurrent().isType(SymbolType.Newline)) row = null;
        }
    }
}
