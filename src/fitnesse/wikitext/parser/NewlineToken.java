package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class NewlineToken extends Token {
    public NewlineToken() { super("\n"); }

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(new HtmlTag("br").html());
    }
    public TokenType getType() { return TokenType.Newline; }
}
