package fitnesse.wikitext.parser;

import fitnesse.wikitext.translator.VariableSource;
import util.Maybe;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final Symbol endToken = new Symbol(SymbolType.Empty);

    private ScanString input;
    private Symbol currentToken;
    private int next;
    private TextMaker textMaker;

    public Scanner(String input) {
        this.input = new ScanString(input, 0);
        next = 0;
        textMaker = new TextMaker(new VariableSource() {
            public Maybe<String> findVariable(String name) {
                return Maybe.noString;
            }
        });
    }

    public Scanner(TextMaker textMaker, String input) {
        this.input = new ScanString(input, 0);
        next = 0;
        this.textMaker = textMaker;
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
    public Symbol getCurrent() { return currentToken; }

    public void copy(Scanner other) {
        input = new ScanString(other.input);
        next = other.next;
        currentToken = other.currentToken;
        textMaker = other.textMaker;
    }

    public List<Symbol> nextTokens(SymbolType[] symbolTypes) {
        ArrayList<Symbol> tokens = new ArrayList<Symbol>();
        for (SymbolType type: symbolTypes) {
            moveNext();
            if (!isType(type)) return new ArrayList<Symbol>();
            tokens.add(getCurrent());
        }
        return tokens;
    }

    public SymbolType makeLiteral(SymbolType terminator) {
        input.setOffset(next);
        while (!input.isEnd()) {
            TokenMatch match = terminator.makeMatch(input);
            if (match.isMatch()) {
                currentToken = new Symbol(SymbolType.Text, input.substringFrom(next));
                next = input.getOffset();
                return terminator;
            }
            input.moveNext();
        }
        currentToken = new Symbol(SymbolType.Text, input.substringFrom(next));
        next = input.getOffset();
        return SymbolType.Empty;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ArrayList<SymbolType>());
    }

    public void moveNextIgnoreFirst(List<SymbolType> ignoreFirst) {
        moveNextIgnoreFirst(new SymbolProvider(), ignoreFirst);
    }

    public void moveNextIgnoreFirst(SymbolProvider provider, List<SymbolType> ignoreFirst) {
        input.setOffset(next);
        int newNext = next;
        Symbol matchToken = null;
        while (!input.isEnd()) {
            for (SymbolType candidate: provider.candidates(input.charAt(0))) {
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
            TokenMatch match = textMaker.make(provider, input.substringFrom(next));
            currentToken = match.getToken();
            next += match.getMatchLength();
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
