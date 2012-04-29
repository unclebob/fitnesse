package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final Symbol emptyToken = new Symbol(SymbolType.Empty);

    private ScanString input;
    private Symbol currentToken;
    private Symbol previousToken;
    private int next;
    private TextMaker textMaker;

    public Scanner(SourcePage sourcePage, String input) {
        this.input = new ScanString(input, 0);
        next = 0;
        textMaker = new TextMaker(new VariableSource() {
                public Maybe<String> findVariable(String name) {
                    return Maybe.noString;
                }
            }, sourcePage);
        previousToken = emptyToken;
        currentToken = emptyToken;
    }

    public Scanner(TextMaker textMaker, String input) {
        this.input = new ScanString(input, 0);
        next = 0;
        this.textMaker = textMaker;
        previousToken = emptyToken;
        currentToken = emptyToken;
    }

    public Scanner(Scanner other) {
        copy(other);
    }

    public int getOffset() { return next; }
    public void markStart() { input.markStart(next); }

    public boolean isEnd() { return currentToken == emptyToken; }
    public boolean isLast() { return input.isEnd(1); }
    public Symbol getCurrent() { return currentToken; }
    public boolean isPrevious(SymbolType type) { return previousToken.isType(type); }

    public Maybe<String> stringFromStart(int start) {
        int end = getOffset() - getCurrent().getContent().length();
        return start <= end
            ? new Maybe<String>(input.rawSubstring(start, end))
            : Maybe.noString;
    }

    public void copy(Scanner other) {
        input = new ScanString(other.input);
        next = other.next;
        currentToken = other.currentToken;
        previousToken = other.previousToken;
        textMaker = other.textMaker;
    }

    public Symbol makeLiteral(SymbolType terminator) {
        input.setOffset(next);
        while (!input.isEnd()) {
            SymbolMatch match = terminator.makeMatch(input);
            if (match.isMatch()) {
                Symbol result = new Symbol(SymbolType.Text, input.substringFrom(next));
                next = input.getOffset() + match.getMatchLength();
                return result;
            }
            input.moveNext();
        }
        Symbol result = new Symbol(SymbolType.Text, input.substringFrom(next));
        next = input.getOffset();
        setCurrentToken(emptyToken);
        return result;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ParseSpecification());
    }

    public void moveNextIgnoreFirst(ParseSpecification specification) {
        Step step = makeNextStep(specification, next);
        next = step.nextPosition;
        setCurrentToken(step.token);
    }

    public List<Symbol> peek(int count, ParseSpecification specification) {
        List<Symbol> result = new ArrayList<Symbol>();
        int startPosition = next;
        for (int i = 0; i < count; i++) {
            Step step = makeNextStep(specification, startPosition);
            result.add(step.token);
            if (input.isEnd()) break;
            startPosition = step.nextPosition;
        }
        return result;
    }

    private void setCurrentToken(Symbol value)  {
        previousToken = currentToken;
        currentToken = value;
    }

    private Step makeNextStep(final ParseSpecification specification, final int startPosition) {
        input.setOffset(startPosition);
        int newNext = startPosition;
        Symbol matchSymbol = null;
        while (!input.isEnd()) {
            SymbolMatch match = specification.findMatch(input, new MatchableFilter() {
                public boolean isValid(Matchable candidate) {
                    return input.getOffset() != startPosition || !specification.ignores(candidate);
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
            SymbolMatch match = textMaker.make(specification, input.substringFrom(startPosition));
            return new Step(match.getSymbol(), startPosition + match.getMatchLength());
        }
        if (input.isEnd()) {
            return new Step(emptyToken, input.getOffset());
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
}
