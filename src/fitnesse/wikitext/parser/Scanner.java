package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final Symbol endToken = new Symbol(SymbolType.Empty);

    private ScanString input;
    private Symbol currentToken;
    private int next;
    private TextMaker textMaker;

    public Scanner(SourcePage sourcePage, String input) {
        this.input = new ScanString(input, 0);
        next = 0;
        textMaker = new TextMaker(new VariableSource() {
            public Maybe<String> findVariable(String name) {
                return Maybe.noString;
            }
        },
        sourcePage);
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
    public boolean isLast() { return input.isEnd(1); }
    public Symbol getCurrent() { return currentToken; }

    public void copy(Scanner other) {
        input = new ScanString(other.input);
        next = other.next;
        currentToken = other.currentToken;
        textMaker = other.textMaker;
    }

    public Symbol makeLiteral(SymbolType terminator) {
        input.setOffset(next);
        while (!input.isEnd()) {
            SymbolMatch match = new SymbolProvider(new SymbolType[] {terminator}).findMatch(input, new MatchableFilter() {
                public boolean isValid(Matchable candidate) {
                    return true;
                }
            });
            if (match.isMatch()) {
                Symbol result = new Symbol(SymbolType.Text, input.substringFrom(next));
                next = input.getOffset() + match.getMatchLength();
                return result;
            }
            input.moveNext();
        }
        Symbol result = new Symbol(SymbolType.Text, input.substringFrom(next));
        next = input.getOffset();
        currentToken = endToken;
        return result;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ArrayList<SymbolType>());
    }

    public void moveNextIgnoreFirst(List<SymbolType> ignoreFirst) {
        moveNextIgnoreFirst(SymbolProvider.wikiParsingProvider, ignoreFirst);
    }

    public void moveNextIgnoreFirst(SymbolProvider provider, List<SymbolType> ignoreFirst) {
        Step step = makeNextStep(provider, ignoreFirst, next);
        next = step.nextPosition;
        currentToken = step.token;
    }

    public List<Symbol> peek(int count, SymbolProvider provider, List<SymbolType> ignoreFirst) {
        List<Symbol> result = new ArrayList<Symbol>();
        int startPosition = next;
        for (int i = 0; i < count; i++) {
            Step step = makeNextStep(provider, ignoreFirst, startPosition);
            result.add(step.token);
            if (input.isEnd()) break;
            startPosition = step.nextPosition;
        }
        return result;
    }

    private Step makeNextStep(SymbolProvider provider, final List<SymbolType> ignoreFirst, final int startPosition) {
        input.setOffset(startPosition);
        int newNext = startPosition;
        Symbol matchSymbol = null;
        while (!input.isEnd()) {
            SymbolMatch match = provider.findMatch(input, new MatchableFilter() {
                public boolean isValid(Matchable candidate) {
                    return input.getOffset() != startPosition || !contains(ignoreFirst, candidate);
                }
            });
            if (match.isMatch()) {
                matchSymbol = match.getSymbol();
                newNext = input.getOffset() + match.getMatchLength();
                break;
            }
            input.moveNext();
        }
        if (input.getOffset() > startPosition) {
            SymbolMatch match = textMaker.make(provider, input.substringFrom(startPosition));
            return new Step(match.getSymbol(), startPosition + match.getMatchLength());
        }
        if (input.isEnd()) {
            return new Step(endToken, input.getOffset());
        }
        return new Step(matchSymbol, newNext);
    }


    private class Step {
        public Symbol token;
        public int nextPosition;
        public Step(Symbol token, int nextPosition) {
            this.token = token;
            this.nextPosition = nextPosition;
        }
    }

    private boolean contains(List<SymbolType> ignoreList, Matchable candidate) {
        for (SymbolType ignore: ignoreList) {
            if (candidate.matchesFor(ignore)) return true;
        }
        return false;
    }
}
