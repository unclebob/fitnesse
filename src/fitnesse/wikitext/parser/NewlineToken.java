package fitnesse.wikitext.parser;

public class NewlineToken extends ContentTypeToken {
    public NewlineToken() { super("\n"); }

    public TokenType getType() { return TokenType.Newline; }
}
