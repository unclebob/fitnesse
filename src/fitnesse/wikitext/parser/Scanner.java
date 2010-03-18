package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private static final Token[] tokens = {
            new EqualPairToken("'''", "b", ""),
            new EqualPairToken("''", "i", ""),
            new EqualPairToken("--", "span", "strike"),
            new StyleToken(),
            new DelimiterToken(")"),
            new DelimiterToken("}"),
            new DelimiterToken("]"),
            new LineToken(),
            new NewlineToken(),
            new AnchorNameToken(),
            new AnchorReferenceToken()
    };
    private static final Token endToken = new EmptyToken();

    private String input;
    private Token activeToken;
    private int next;

    public Scanner(String input) {
        this.input = input;
        next = 0;
    }

    public Scanner(Scanner other) {
        copy(other);
    }

    public void copy(Scanner other) {
        input = other.input;
        next = other.next;
        activeToken = other.activeToken;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ArrayList<Token>());
    }

    public void moveNextIgnoreFirst(List<Token> ignoreFirst) {
        int scan = next;
        int newNext = next;
        Token matchToken = null;
        while (scan < input.length()) {
            for (Token candidate: tokens) {
                if (scan != next || !contains(ignoreFirst, candidate)) {
                    TokenMatch match = candidate.makeMatch(new ScanString(input, scan));
                    if (match.isMatch()) {
                        matchToken = match.getToken();
                        newNext = scan + match.getMatchLength();
                        break;
                    }
                }
            }
            if (matchToken != null) break;
            scan++;
        }
        if (scan >= input.length()) {
            matchToken = endToken;
            newNext = scan;
        }
        if (scan > next) {
            activeToken = new TextToken(input.substring(next, scan));
            next = scan;
        }
        else {
            activeToken = matchToken;
            next = newNext;
        }
    }

    private boolean contains(List<Token> ignoreList, Token candidate) {
        for (Token ignore: ignoreList) {
            if (ignore.sameAs(candidate)) return true;
        }
        return false;
    }

    public boolean isEnd() { return activeToken == endToken; }
    public Token getCurrent() { return activeToken; }
}
