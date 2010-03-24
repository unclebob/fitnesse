package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final Token endToken = new EmptyToken();

    private String input;
    private Token currentToken;
    private int next;

    public Scanner(String input) {
        this.input = input;
        next = 0;
    }

    public Scanner(Scanner other) {
        copy(other);
    }

    public int getOffset() { return next; }
    public String substring(int startAt, int endBefore) { return input.substring(startAt, endBefore); }
    public boolean isEnd() { return currentToken == endToken; }
    public boolean isType(TokenType type) { return currentToken.getType() == type; }
    public String getCurrentContent() { return currentToken.getContent(); }
    public Token getCurrent() { return currentToken; }

    public void copy(Scanner other) {
        input = other.input;
        next = other.next;
        currentToken = other.currentToken;
    }

    public List<Token> nextTokens(TokenType[] tokenTypes) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        for (TokenType type: tokenTypes) {
            moveNext();
            if (!isType(type)) return new ArrayList<Token>();
            tokens.add(getCurrent());
        }
        return tokens;
    }

    public void makeLiteral(TokenType terminator) {
        int scan = next;
        while (scan < input.length()) {
            TokenMatch match = terminator.makeMatch(new ScanString(input, scan));
            if (match.isMatch()) {
                currentToken = new TextToken(input.substring(next, scan));
                next = scan;
                return;
            }
            scan++;
        }
        currentToken = endToken;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ArrayList<TokenType>());
    }

    public void moveNextIgnoreFirst(List<TokenType> ignoreFirst) {
        int scan = next;
        int newNext = next;
        Token matchToken = null;
        while (scan < input.length()) {
            for (TokenType candidate: TokenType.values()) {
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
            currentToken = new TextToken(input.substring(next, scan));
            next = scan;
        }
        else {
            currentToken = matchToken;
            next = newNext;
        }
    }

    private boolean contains(List<TokenType> ignoreList, TokenType candidate) {
        for (TokenType ignore: ignoreList) {
            if (ignore == candidate) return true;
        }
        return false;
    }
}
