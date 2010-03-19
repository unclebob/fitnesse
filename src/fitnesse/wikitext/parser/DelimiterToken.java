package fitnesse.wikitext.parser;

public class DelimiterToken extends ContentTypeToken {
    public static final DelimiterToken CloseParenthesisToken = new DelimiterToken(")");
    public static final DelimiterToken CloseBraceToken = new DelimiterToken("}");
    public static final DelimiterToken CloseBracketToken = new DelimiterToken("]");

    public DelimiterToken(String content) {
        super(content);
    }
}
