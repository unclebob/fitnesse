package fitnesse.wikitext.parser;

public class CellDelimiterToken extends Token {
    public CellDelimiterToken(String content) { super(content); }

    public TokenType getType() { return TokenType.EndCell; }
}
