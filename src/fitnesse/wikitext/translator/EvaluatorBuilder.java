package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;
import util.FormattedExpression;
import util.Maybe;

public class EvaluatorBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        String body = translator.translate(symbol.childAt(0));
        Maybe<String> result = new FormattedExpression(body).evaluate();
        if (result.isNothing()) return translator.formatMessage(result.because());
        return result.getValue().toString();
    }
}
