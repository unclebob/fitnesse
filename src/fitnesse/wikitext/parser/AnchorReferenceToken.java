package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorReferenceToken extends Token {
    public AnchorReferenceToken() { super(); }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Word) return Maybe.noString;
        return new Maybe<String>(HtmlUtil.makeLink("#" + scanner.getCurrent().getContent(), ".#" + scanner.getCurrent().getContent()).html());
    }
    
    public TokenType getType() { return TokenType.AnchorReference; }
}
