package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.util.List;

public class AnchorNameToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new TokenType[] {TokenType.Whitespace, TokenType.Text});
        if (tokens.size() == 0) return Maybe.noString;

        String anchor = tokens.get(1).getContent();
        if (!ScanString.isWord(anchor)) return Maybe.noString;

        return new Maybe<String>(HtmlUtil.makeAnchorTag(anchor).html());
    }
}
