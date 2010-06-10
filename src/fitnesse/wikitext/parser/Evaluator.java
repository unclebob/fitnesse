package fitnesse.wikitext.parser;

import util.FormattedExpression;
import util.Maybe;

public class Evaluator extends SymbolType implements Rule, Translation {
    public static final Evaluator symbolType = new Evaluator();
    
    public Evaluator() {
        super("Evaluator");
        wikiMatcher(new Matcher().string("${="));
        wikiRule(this);
        htmlTranslation(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseTo(SymbolType.CloseEvaluator);
        if (parser.atEnd()) return Symbol.nothing;
        return new Maybe<Symbol>(current.add(body));
    }

    public String toTarget(Translator translator, Symbol symbol) {
        String body = translator.translate(symbol.childAt(0));
        Maybe<String> result = new FormattedExpression(body).evaluate();
        if (result.isNothing()) return translator.formatMessage(result.because());
        return result.getValue();
    }
}
