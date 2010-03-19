package fitnesse.wikitext.parser;

public abstract class ContentTypeToken extends ContentToken {
    private TokenType type;

    public ContentTypeToken(String content) { super(content); }
    public void setType(TokenType type) { this.type = type; }
    public TokenType getType() { return type; }
}
