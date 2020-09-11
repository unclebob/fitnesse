package fitnesse.wikitext.parser;

import fitnesse.wikitext.SourcePage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scanner {

    private ScanString input;
    private int next;
    private TextMaker textMaker;
    private SymbolStream symbols;

    public Scanner(SourcePage sourcePage, CharSequence input) {
        this(
            new TextMaker(name -> Optional.empty(), sourcePage),
            input);
    }

    public Scanner(TextMaker textMaker, CharSequence input) {
        this.input = new ScanString(input, 0);
        next = 0;
        this.textMaker = textMaker;
        symbols = new SymbolStream();
    }

    public Scanner(Scanner other) {
        copy(other);
    }

    public int getOffset() { return next; }
    public void markStart() { input.markStart(next); }
    public boolean isEnd() { return symbols.isEnd(); }
    public Symbol getCurrent() { return symbols.get(0); }

    public Maybe<String> stringFromStart(int start) {
        int end = getOffset() - getCurrent().getContent().length();
        return start <= end
            ? new Maybe<>(input.rawSubstring(start, end))
            : Maybe.noString;
    }

    public void copy(Scanner other) {
        input = new ScanString(other.input);
        next = other.next;
        textMaker = other.textMaker;
        symbols = new SymbolStream(other.symbols);
    }

    public Symbol makeLiteral(SymbolType terminator) {
        input.setOffset(next);
        while (!input.isEnd()) {
            SymbolMatch match = terminator.makeMatch(input, symbols);
            if (match.isMatch()) {
                symbols.add(new Symbol(terminator));
                Symbol result = new Symbol(SymbolType.Text, input.substringFrom(next), next);
                next = input.getOffset() + match.getMatchLength();
                return result;
            }
            input.moveNext();
        }
        Symbol result = new Symbol(SymbolType.Text, input.substringFrom(next), next);
        next = input.getOffset();
        symbols.add(Symbol.emptySymbol);
        return result;
    }

    public void moveNext() {
        moveNextIgnoreFirst(new ParseSpecification());
    }

    public void moveNextIgnoreFirst(ParseSpecification specification) {
        Step step = makeNextStep(specification, next);
        next = step.nextPosition;
        symbols.add(step.token);
    }

    public List<Symbol> peek(int count, ParseSpecification specification) {
        List<Symbol> result = new ArrayList<>(count);
        int startPosition = next;
        for (int i = 0; i < count; i++) {
            Step step = makeNextStep(specification, startPosition);
            result.add(step.token);
            if (input.isEnd()) break;
            startPosition = step.nextPosition;
        }
        return result;
    }

    private Step makeNextStep(final ParseSpecification specification, final int startPosition) {
        input.setOffset(startPosition);
        int newNext = startPosition;
        Symbol matchSymbol = null;
        while (!input.isEnd()) {
            SymbolMatch match = specification.findMatch(input, startPosition, symbols);
            if (match.isMatch()) {
                matchSymbol = match.getSymbol();
                newNext = input.getOffset() + match.getMatchLength();
                break;
            }
            input.moveNext();
        }
        if (input.getOffset() > startPosition) {
            SymbolMatch match = textMaker.make(specification, startPosition, input.substringFrom(startPosition));
            return new Step(match.getSymbol(), startPosition + match.getMatchLength());
        }
        if (input.isEnd()) {
            return new Step(Symbol.emptySymbol, input.getOffset());
        }
        return new Step(matchSymbol, newNext);
    }

    private static class Step {
        public Symbol token;
        public int nextPosition;
        public Step(Symbol token, int nextPosition) {
            this.token = token;
            this.nextPosition = nextPosition;
        }
    }
}
