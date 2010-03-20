package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

public class Token {
    private TokenType type;
    private String content = "";
    private WikiPage page;

    public Token() {}
    public Token(TokenType type) { this.type = type; }
    public Token(String content) { this.content = content;  }
    public Token(TokenType type, String content) {
        this.type = type;
        this.content = content;
    }

    public Maybe<String> render(Scanner scanner) { return new Maybe<String>(content); }

    public TokenType getType() { return type; }
    public void setType(TokenType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public WikiPage getPage() { return page; }
    public void setPage(WikiPage page) { this.page = page; }
}
