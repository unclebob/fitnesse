package fitnesse.wikitext.parser;

import util.Maybe;

public class ContentToken extends TokenBase {
    private String content;

    public ContentToken() { this(""); }
    public ContentToken(String content) { this.content = content;  }

    public ContentToken(TokenType type, String content) {
        super(type);
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(content);
    }

    public String toString() { return content; }
}
