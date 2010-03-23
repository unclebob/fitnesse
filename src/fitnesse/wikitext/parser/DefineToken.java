package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.util.List;

public class DefineToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new TokenType[] {TokenType.Whitespace, TokenType.Text, TokenType.Whitespace});
        if (tokens.size() == 0) return Maybe.noString;

        String name = tokens.get(1).getContent();
        if (!ScanString.isWord(name)) return Maybe.noString;

        scanner.moveNext();
        TokenType open = scanner.getCurrent().getType();
        TokenType close = TokenType.closeType(open);
        if (close == TokenType.Empty) return Maybe.noString;

        int start = scanner.getOffset();
        String value = new Translator(getPage()).translateIgnoreFirst(scanner, close);
        if (scanner.isEnd()) return Maybe.noString;

        try {
            getPage().getData().addVariable(name, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new Maybe<String>(HtmlUtil.metaText("variable defined: "
                + name + "=" + scanner.substring(start, scanner.getOffset() - 1)));
    }
}
