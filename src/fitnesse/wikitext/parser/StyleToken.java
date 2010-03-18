package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import util.Maybe;

public class StyleToken extends ContentToken {
    private static final String delimiter = "!style_";
    private static final char[] beginners = {'(', '{', '['};

    private final String terminator;

    public StyleToken() { this("", ""); }

    public StyleToken(String content, String terminator) {
        super(content);
        this.terminator = terminator;
    }

    public TokenMatch makeMatch(ScanString input) {
        if (input.startsWith(delimiter)) {
            int beginner = input.find(beginners, delimiter.length());
            if (beginner > delimiter.length()) {
                return new TokenMatch(
                        new StyleToken(input.substring(delimiter.length(), beginner),
                                makeTerminator(input.charAt(beginner))),
                        beginner + 1);
            }
        }
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator().translate(scanner, new DelimiterToken(terminator));
        if (scanner.isEnd()) return Maybe.noString;
        return new Maybe<String>(HtmlUtil.makeSpanTag(getContent(), body).html());
    }

    private String makeTerminator(char beginner) {
        return beginner == '[' ? "]" : beginner == '{' ? "}" : ")";
    }

    public boolean sameAs(Token other) {
        return other instanceof StyleToken;
    }
}
