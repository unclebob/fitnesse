package fitnesse.wikitext.parser;

public class TextToken extends Token {
    public TextToken(String content) { super(content); }

    public TokenType getType() { return TokenType.Text; }
}
