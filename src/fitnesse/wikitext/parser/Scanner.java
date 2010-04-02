package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final Token endToken = new Token(SymbolType.Empty);

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
    public boolean isType(SymbolType type) { return currentToken.getType() == type; }
    public String getCurrentContent() { return currentToken.getContent(); }
    public SymbolType getCurrentType() { return currentToken.getType(); }
    public Token getCurrent() { return currentToken; }

    public void copy(Scanner other) {
        input = new ScanString(other.input);
        next = other.next;
        currentToken = other.currentToken;
    }

    public List<Token> nextTokens(SymbolType[] symbolTypes) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        for (SymbolType type: symbolTypes) {
            moveNext();
            if (!isType(type)) return new ArrayList<Token>();
            tokens.add(getCurrent());
        }
        return tokens;
    }

    public void makeLiteral(SymbolType terminator) {
        input.setOffset(next);
        while (!input.isEnd()) {
            TokenMatch match = terminator.makeMatch(input);
            if (match.isMatch()) {
                currentToken = new Token(SymbolType.Text, input.substringFrom(next));
                next = input.getOffset();
                return;
            }
            input.moveNext();
        }
        currentToken = endToken;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ArrayList<SymbolType>());
    }

    public void moveNextIgnoreFirst(List<SymbolType> ignoreFirst) {
        input.setOffset(next);
        int newNext = next;
        Token matchToken = null;
        while (!input.isEnd()) {
            for (SymbolType candidate: SymbolType.getMatchTypes(input.charAt(0))) {
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
            currentToken =  new TextMaker().makeToken(input.substringFrom(next));
            next = input.getOffset();
        }
        else {
            currentToken = matchToken;
            next = newNext;
        }
    }

    private boolean contains(List<SymbolType> ignoreList, SymbolType candidate) {
        for (SymbolType ignore: ignoreList) {
            if (ignore == candidate) return true;
        }
        return false;
    }
}
