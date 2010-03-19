package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorNameToken extends ContentToken {
    public AnchorNameToken(String content) { super(content); }

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(HtmlUtil.makeAnchorTag(getContent()).html());
    }

    public TokenType getType() { return TokenType.AnchorName; }
}
