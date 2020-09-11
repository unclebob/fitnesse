package fitnesse.wikitext.parser;

import fitnesse.wikitext.ParsingPage;
import fitnesse.wikitext.VariableSource;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static final ArrayList<Symbol> emptySymbols = new ArrayList<>();

    public static Parser make(ParsingPage currentPage, CharSequence input) {
        return make(currentPage, input, SymbolProvider.wikiParsingProvider);
    }

    public static Parser make(ParsingPage currentPage, CharSequence input, SymbolProvider provider) {
        ParseSpecification specification = new ParseSpecification().provider(provider);
        return new Parser(null, currentPage, new Scanner(new TextMaker(currentPage, currentPage.getNamedPage()), input), specification);
    }

    private ParsingPage currentPage;
    private Scanner scanner;
    private Parser parent;
    private ParseSpecification specification;

    public Parser(Parser parent, ParsingPage currentPage, Scanner scanner, ParseSpecification specification) {
        this.parent = parent;
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.specification = specification;
    }

    public ParsingPage getPage() { return currentPage; }
    public VariableSource getVariableSource() { return currentPage; }
    public Symbol getCurrent() { return scanner.getCurrent(); }
    public int getOffset() { return scanner.getOffset(); }
    public boolean atEnd() { return scanner.isEnd(); }
    public boolean isMoveNext(SymbolType type) { return moveNext(1).isType(type); }
    public boolean endsOn(SymbolType type) { return specification.endsOn(type); }

    public Symbol moveNext(int count) {
        for (int i = 0; i < count; i++) scanner.moveNextIgnoreFirst(specification);
        return scanner.getCurrent();
    }

    public List<Symbol> moveNext(SymbolType[] symbolTypes) {
        ArrayList<Symbol> tokens = new ArrayList<>();
        for (SymbolType type: symbolTypes) {
            Symbol current = moveNext(1);
            if (!current.isType(type)) return new ArrayList<>();
            tokens.add(current);
        }
        return tokens;
    }

    public Symbol peek() {
        return peek(1).get(0);
    }

    public List<Symbol> peek(int size) {
        return scanner.peek(size, new ParseSpecification().provider(specification));
    }

    public List<Symbol> peek(SymbolType[] types) {
        List<Symbol> lookAhead = scanner.peek(types.length, new ParseSpecification().provider(specification));
        if (lookAhead.size() != types.length) return emptySymbols;
        for (int i = 0; i < lookAhead.size(); i++) {
            if (!lookAhead.get(i).isType(types[i])) return emptySymbols;
        }
        return lookAhead;
    }

    public Maybe<String> parseToAsString(SymbolType terminator) {
        int start = scanner.getOffset();
        scanner.markStart();
        parseTo(terminator);
        if (!atEnd() && !getCurrent().isType(terminator)) return Maybe.noString;
        return scanner.stringFromStart(start);
    }

    public String parseLiteral(SymbolType terminator) {
        return scanner.makeLiteral(terminator).getContent();
    }

    public Symbol parse(String input) {
        return parseWithParent(input, this);
    }

    public Symbol parseWithParent(String input, Parser parent) {
        return new Parser(parent, currentPage, new Scanner(new TextMaker(currentPage, currentPage.getNamedPage()), input), new ParseSpecification().provider(specification)).parse();
    }

    public Symbol parseToIgnoreFirst(SymbolType type) {
        return parseToIgnoreFirst(new SymbolType[] {type});
    }

    public Symbol parseToIgnoreFirst(SymbolType[] types) {
        ParseSpecification newSpecification = new ParseSpecification().provider(specification);
        for (SymbolType symbolType: types) {
            newSpecification.terminator(symbolType);
            newSpecification.ignoreFirst(symbolType);
        }
        return parse(newSpecification);
    }

    public Symbol parseToIgnoreFirstWithSymbols(SymbolType ignore, SymbolProvider provider) {
        return parse(new ParseSpecification().ignoreFirst(ignore).terminator(ignore).provider(provider));
    }

    public Symbol parseTo(SymbolType terminator) {
        return parseTo(terminator, ParseSpecification.normalPriority);
    }

    public Symbol parseTo(SymbolType terminator, int priority) {
        return parse(new ParseSpecification() /*.provider(specification)*/ .terminator(terminator).priority(priority));
    }

    public Symbol parseToWithSymbols(SymbolType terminator, SymbolProvider provider, int priority) {
        SymbolType[] terminators = new SymbolType[] {terminator};
        return parseToWithSymbols(terminators, provider, priority);
    }

    public Symbol parseToWithSymbols(SymbolType[] terminators, SymbolProvider provider, int priority) {
        ParseSpecification newSpecification = new ParseSpecification().provider(provider).priority(priority);
        for (SymbolType terminator: terminators) newSpecification.terminator(terminator);
        return parse(newSpecification);
    }

    public Symbol parseToEnd(SymbolType end) {
        return parse(new ParseSpecification().provider(specification).end(end));
    }

    public Symbol parseToEnds(int priority, SymbolProvider provider, SymbolType[] moreEnds) {
        ParseSpecification newSpecification = specification.makeSpecification(provider, moreEnds).priority(priority);
        for (SymbolType end: moreEnds) newSpecification.end(end);
        return parse(newSpecification);
    }

    private Symbol parse(ParseSpecification newSpecification) {
        return new Parser(this, currentPage, scanner, newSpecification).parse();
    }

    public Symbol parse() {
        return specification.parse(this, scanner);
    }

    public boolean parentOwns(SymbolType current, ParseSpecification specification) {
        if (parent == null) return false;
        if (parent.specification.owns(current, specification)) return true;
        return parent.parentOwns(current, specification);
    }
}
