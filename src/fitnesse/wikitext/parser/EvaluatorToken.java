package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.translator.Translator;
import util.Expression;
import util.Maybe;

public class EvaluatorToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, SymbolType.CloseEvaluator);
        if (scanner.isEnd()) return Maybe.noString;

        Maybe<Double> result = new Expression(body).evaluate();
        if (result.isNothing()) return new Maybe<String>(HtmlUtil.metaText("invalid expression: " + body));

        Long iResult = Math.round(result.getValue());
        return new Maybe<String>(result.getValue().equals(iResult.doubleValue()) ? iResult.toString() : result.toString());
    }
}
