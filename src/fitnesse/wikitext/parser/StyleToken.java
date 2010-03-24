package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class StyleToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        char beginner = getContent().charAt(getContent().length() - 1);
        String style = getContent().substring(7, getContent().length() - 1);
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, TokenType.closeType(beginner));
        if (scanner.isEnd()) return Maybe.noString;
        return new Maybe<String>(HtmlUtil.makeSpanTag(style, body).html());
    }

    public TokenType getType() { return TokenType.Style; }
}
