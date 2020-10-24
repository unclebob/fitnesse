package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.Names;

public class LineRule implements Rule {

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        String level = current.getContent().substring(1,2);
        if (ScanString.isDigits(level)) { //todo: a bit of a hack - this is for header lines
          current.putProperty(Names.LEVEL, level);
          current.putProperty(Names.ID, Integer.toString(parser.getPage().nextId()));
        }

        current.add(parser.parseToEnd(SymbolType.Newline));
        if (parser.peek().isType(SymbolType.Newline) && !parser.endsOn(SymbolType.Newline)) parser.moveNext(1);
        return new Maybe<>(current);
    }
}
