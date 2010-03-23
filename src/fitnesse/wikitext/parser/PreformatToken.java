package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class PreformatToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, TokenType.ClosePreformat);
        if (scanner.isEnd()) return Maybe.noString;

        return new Maybe<String>(new HtmlTag("pre", body).html());
    }
}
