package fitnesse.wikitext.parser;

import util.Maybe;

public class TokenBase implements Token {
    private TokenType type;

    public TokenBase() { type = TokenType.Empty; }
    public TokenBase(TokenType type)  { this.type = type; }

    public Maybe<String> render(Scanner scanner) { return Maybe.noString; }

    public TokenType getType() { return type; }
    public void setType(TokenType type) { this.type = type; }

    public String toString() { return ""; }
}
