package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final Token endToken = new EmptyToken();

    private ScanString input;
    private Token currentToken;
    private int next;

    public Scanner(String input) {
        this.input = new ScanString(input, 0);
        next = 0;
    }

    public Scanner(Scanner other) {
        copy(other);
    }

    public int getOffset() { return next; }
    public void markStart() { input.markStart(next); }
    public String substring(int startAt, int endBefore) { return input.rawSubstring(startAt, endBefore); }
    public boolean isEnd() { return currentToken == endToken; }
    public boolean isType(TokenType type) { return currentToken.getType() == type; }
    public String getCurrentContent() { return currentToken.getContent(); }
    public Token getCurrent() { return currentToken; }

    public void copy(Scanner other) {
        input = new ScanString(other.input);
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
        input.setOffset(next);
        while (!input.isEnd()) {
            TokenMatch match = terminator.makeMatch(input);
            if (match.isMatch()) {
                currentToken = new TextToken(input.substringFrom(next));
                next = input.getOffset();
                return;
            }
            input.moveNext();
        }
        currentToken = endToken;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ArrayList<TokenType>());
    }

    public void moveNextIgnoreFirst(List<TokenType> ignoreFirst) {
        input.setOffset(next);
        int newNext = next;
        Token matchToken = null;
        while (!input.isEnd()) {
            for (TokenType candidate: TokenType.getMatchTypes(input.charAt(0))) {
                if (input.getOffset() != next || !contains(ignoreFirst, candidate)) {
                    TokenMatch match = candidate.makeMatch(input);
                    if (match.isMatch()) {
                        matchToken = match.getToken();
                        newNext = input.getOffset() + match.getMatchLength();
                        break;
                    }
                }
            }
            if (matchToken != null) break;
            input.moveNext();
        }
        if (input.isEnd()) {
            matchToken = endToken;
            newNext = input.getOffset();
        }
        if (input.getOffset() > next) {
            currentToken = new TextToken(input.substringFrom(next));
            next = input.getOffset();
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
