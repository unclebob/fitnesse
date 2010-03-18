package fitnesse.wikitext.parser;

public class DelimiterToken extends ContentToken {
    public DelimiterToken(String content) {
        super(content);
    }

    public boolean sameAs(Token other) {
        return other instanceof DelimiterToken && ((DelimiterToken)other).getContent().equals(getContent());
    }
}
