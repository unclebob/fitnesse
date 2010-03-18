package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class AnchorNameToken extends ContentToken {
    private static final String delimiter = "!anchor ";

    public AnchorNameToken() { this(""); }
    public AnchorNameToken(String content) { super(content); }

    public TokenMatch makeMatch(ScanString input) {
        if (input.startsWith(delimiter)) {
            int wordLength = input.wordLength(delimiter.length());
            if (wordLength > 0) {
                return new TokenMatch(
                        new AnchorNameToken(input.substring(delimiter.length(), delimiter.length() + wordLength)),
                        delimiter.length() + wordLength);
            }
        }
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        return new Maybe<String>(HtmlUtil.makeAnchorTag(getContent()).html());
        //return new Maybe<String>("<a name=\"" + getContent() + "\"> </a>");
    }
    
    public boolean sameAs(Token other) {
        return other instanceof AnchorNameToken;
    }
}
