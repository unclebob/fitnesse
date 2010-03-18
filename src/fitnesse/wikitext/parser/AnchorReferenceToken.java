package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorReferenceToken extends ContentToken {
    private static final String delimiter = ".#";

    public AnchorReferenceToken() { this(""); }
    public AnchorReferenceToken(String content) { super(content); }

    public boolean sameAs(Token other) {
        return other instanceof AnchorReferenceToken;
    }

    public TokenMatch makeMatch(ScanString input) {
        if (input.startsWith(delimiter)) {
            int wordLength = input.wordLength(delimiter.length());
            if (wordLength > 0) {
                return new TokenMatch(
                        new AnchorReferenceToken(input.substring(delimiter.length(), delimiter.length() + wordLength)),
                        delimiter.length() + wordLength);
            }
        }
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(HtmlUtil.makeLink("#" + getContent(), ".#" + getContent()).html());
    }
}
