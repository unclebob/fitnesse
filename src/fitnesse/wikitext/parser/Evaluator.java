package fitnesse.wikitext.parser;

public class Evaluator extends SymbolType implements Rule, Translation {
    public static final Evaluator symbolType = new Evaluator();

    public Evaluator() {
        super("Evaluator");
        wikiMatcher(new Matcher().string("${="));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseTo(SymbolType.CloseEvaluator);
        if (parser.atEnd()) return Symbol.nothing;
        return new Maybe<>(current.add(body));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String body = translator.translate(symbol.childAt(0));
        Maybe<String> formatLocale = Maybe.noString;
        if(translator instanceof HtmlTranslator){
          formatLocale = ((HtmlTranslator) translator).getParsingPage().findVariable("FORMAT_LOCALE");
        }
        Maybe<String> result = new FormattedExpression(body, formatLocale).evaluate();
        if (result.isNothing()) return translator.formatMessage(result.because());
        return result.getValue();
    }
}
