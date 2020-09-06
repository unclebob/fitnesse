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
        current.evaluateVariables(new String[] {EVALUATOR_LOCALE}, parser.getVariableSource());
        return new Maybe<>(current.add(body));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String body = translator.translate(symbol.childAt(0));
        String locale = symbol.getVariable(EVALUATOR_LOCALE, "");
        Maybe<String> formatLocale = locale.length() > 0 ? new Maybe<>(locale) : Maybe.noString;
        Maybe<String> result = new FormattedExpression(body, formatLocale).evaluate();
        if (result.isNothing()) return translator.formatMessage(result.because());
        return result.getValue();
    }

    private static final String EVALUATOR_LOCALE = "FORMAT_LOCALE";
}
