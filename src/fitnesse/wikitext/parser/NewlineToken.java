package fitnesse.wikitext.parser;

public class NewlineToken extends Token {
    public NewlineToken() { super("\n"); }

    public TokenType getType() { return TokenType.Newline; }
}
