package fitnesse.wikitext.parser;

import util.Maybe;

public class Preformat extends SymbolType implements Rule {
    public static final Preformat symbolType = new Preformat();
    private static final SymbolProvider preformatProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.ClosePreformat, SymbolType.CloseBrace, SymbolType.CloseLiteral, Literal.symbolType, Variable.symbolType});

    public Preformat() {
        super("Preformat");
        wikiMatcher(new Matcher().string("{{{"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("pre").body(0));
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol content = parser.parseToWithSymbols(SymbolType.ClosePreformat, preformatProvider, 0);
        if (parser.atEnd())  return Symbol.nothing;
        return new Maybe<Symbol>(current.add(content));
    }
}
