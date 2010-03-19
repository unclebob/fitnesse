package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class StyleToken extends ContentToken {
    private final DelimiterToken terminator;

    public StyleToken(String content, DelimiterToken terminator) {
        super(content);
        this.terminator = terminator;
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator().translate(scanner, terminator.getType());
        if (scanner.isEnd()) return Maybe.noString;
        return new Maybe<String>(HtmlUtil.makeSpanTag(getContent(), body).html());
    }

    public TokenType getType() { return TokenType.Style; }
}
