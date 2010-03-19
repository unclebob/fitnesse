package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorNameToken extends ContentToken {
    public AnchorNameToken() { super(); }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Whitespace) return Maybe.noString;
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Word) return Maybe.noString;
        return new Maybe<String>(HtmlUtil.makeAnchorTag(scanner.getCurrent().toString()).html());
    }

    public TokenType getType() { return TokenType.AnchorName; }
}
