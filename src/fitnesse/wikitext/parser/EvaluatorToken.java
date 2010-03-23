package fitnesse.wikitext.parser;

import util.Expression;
import util.Maybe;

public class EvaluatorToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, TokenType.CloseEvaluator);
        if (scanner.isEnd()) return Maybe.noString;

        try {
            Double result = new Expression(body).evaluate();
            Long iResult = new Long(Math.round(result));
            return new Maybe<String>(result.equals(iResult.doubleValue()) ? iResult.toString() : result.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
