package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class VariableToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new TokenType[] {TokenType.Text, TokenType.CloseBrace});
        if (tokens.size() == 0) return Maybe.noString;

        String name = tokens.get(0).getContent();
        if (!ScanString.isWord(name)) return Maybe.noString;

        try {
            return new Maybe<String>(getPage().getData().getVariable(name));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
