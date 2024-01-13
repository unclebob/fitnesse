package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.ToHtml;

public class Preformat extends SymbolType implements Rule {
    public static final Preformat symbolType = new Preformat();

  public Preformat() {
        super("Preformat");
        wikiMatcher(new Matcher().string("{{{"));
        wikiRule(this);
        htmlTranslation(Translate.with(ToHtml::pair).text("pre").child(0));
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol content = parser.parseToWithSymbols(SymbolType.ClosePreformat, SymbolProvider.preformatProvider, 0);
        if (parser.atEnd())  return Symbol.nothing;
        return new Maybe<>(current.add(content));
    }
}
