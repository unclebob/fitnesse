package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.util.List;

public class AnchorReferenceToken extends Token {
    public AnchorReferenceToken() { super(); }

    public Maybe<String> render(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new TokenType[] {TokenType.Text});
        if (tokens.size() == 0) return Maybe.noString;

        String anchor = tokens.get(0).getContent();
        if (!ScanString.isWord(anchor)) return Maybe.noString;
        
        return new Maybe<String>(HtmlUtil.makeLink("#" + anchor, ".#" + anchor).html());
    }
    
    public TokenType getType() { return TokenType.AnchorReference; }
}
