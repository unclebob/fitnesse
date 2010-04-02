package fitnesse.wikitext.translator;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Symbol;
import util.Expression;
import util.Maybe;

public class EvaluatorBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String body = translator.translate(symbol.childAt(0));
        Maybe<Double> result = new Expression(body).evaluate();
        if (result.isNothing()) return HtmlUtil.metaText("invalid expression: " + body);

        Long iResult = Math.round(result.getValue());
        return result.getValue().equals(iResult.doubleValue()) ? iResult.toString() : result.toString();
    }
}
