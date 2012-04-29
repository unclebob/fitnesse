package fitnesse.wikitext.parser;

import util.Maybe;

public class LineRule implements Rule {
    public static final String Level = "level";

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!isStartOfLine(parser) && !isStartOfCell(parser)) return Symbol.nothing;

        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        String level = current.getContent().substring(1,2);
        if (ScanString.isDigits(level)) current.putProperty(Level, level);

        return new Maybe<Symbol>(current.add(parser.parseToEnd(SymbolType.Newline)));
    }

    private boolean isStartOfCell(Parser parser) {
        return isCellStartAt(parser, 1) ||
                (parser.isTypeAt(1, SymbolType.Whitespace) && isCellStartAt(parser, 2));
    }

    private boolean isCellStartAt(Parser parser, int position) {
        return parser.isTypeAt(position, Table.symbolType) || parser.isTypeAt(position, SymbolType.EndCell);
    }

    private boolean isStartOfLine(Parser parser) {
        return parser.isTypeAt(1, SymbolType.Newline) || parser.isTypeAt(1, SymbolType.Empty);
    }
}
