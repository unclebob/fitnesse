package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorReferenceToken extends ContentToken {
    public AnchorReferenceToken() { super(); }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Word) return Maybe.noString;
        return new Maybe<String>(HtmlUtil.makeLink("#" + scanner.getCurrent().toString(), ".#" + scanner.getCurrent().toString()).html());
    }
    
    public TokenType getType() { return TokenType.AnchorReference; }
}
