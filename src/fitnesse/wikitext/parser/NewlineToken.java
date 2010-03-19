package fitnesse.wikitext.parser;

public class NewlineToken extends ContentToken {
    public NewlineToken() { super("\n"); }

    public TokenType getType() { return TokenType.Newline; }
}
