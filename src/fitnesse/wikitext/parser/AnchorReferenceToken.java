package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorReferenceToken extends ContentToken {
    public AnchorReferenceToken(String content) { super(content); }

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(HtmlUtil.makeLink("#" + getContent(), ".#" + getContent()).html());
    }
    
    public TokenType getType() { return TokenType.AnchorReference; }
}
