package fitnesse.wikitext.parser;

import util.Maybe;

public abstract class ContentToken implements Token {
    private final String content;

    public ContentToken(String content) {
        this.content = content;
    }

    public String getContent() { return content; }

    public TokenMatch makeMatch(ScanString input) {
        return input.startsWith(content)
                ? new TokenMatch(this, content.length())
                : TokenMatch.noMatch;
    }
    
    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(content);
    }

    public String toString() { return content; }
}
