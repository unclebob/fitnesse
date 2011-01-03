package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static final ArrayList<Symbol> emptySymbols = new ArrayList<Symbol>();

    public static Parser make(WikiPage page, String input) {
        return make(new ParsingPage(new WikiSourcePage(page)), input);
    }
    
    public static Parser make(ParsingPage currentPage, String input) {
        return make(currentPage, input, SymbolProvider.wikiParsingProvider);
    }

    public static Parser make(ParsingPage currentPage, String input, SymbolProvider provider) {
        return make(currentPage, input, new VariableFinder(currentPage), provider);
    }

    public static Parser make(ParsingPage currentPage, String input, VariableSource variableSource, SymbolProvider provider) {
        ParseSpecification specification = new ParseSpecification().provider(provider);
        return new Parser(null, currentPage, new Scanner(new TextMaker(variableSource, currentPage.getNamedPage()), input), variableSource, specification);
    }

    private ParsingPage currentPage;
    private VariableSource variableSource;
    private Scanner scanner;
    private Parser parent;
    private ParseSpecification specification;

    public Parser(Parser parent, ParsingPage currentPage, Scanner scanner, VariableSource variableSource, ParseSpecification specification) {
        this.parent = parent;
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.variableSource = variableSource;
        this.specification = specification;
    }

    public ParsingPage getPage() { return currentPage; }
    public VariableSource getVariableSource() { return variableSource; }
    public Symbol getCurrent() { return scanner.getCurrent(); }
    public boolean atEnd() { return scanner.isEnd(); }
    public boolean atLast() { return scanner.isLast(); }
    public boolean isMoveNext(SymbolType type) { return moveNext(1).isType(type); }

    public Symbol moveNext(int count) {
        for (int i = 0; i < count; i++) scanner.moveNext();
        return scanner.getCurrent();
    }

    public List<Symbol> moveNext(SymbolType[] symbolTypes) {
        ArrayList<Symbol> tokens = new ArrayList<Symbol>();
        for (SymbolType type: symbolTypes) {
            Symbol current = moveNext(1);
            if (!current.isType(type)) return new ArrayList<Symbol>();
            tokens.add(current);
        }
        return tokens;
    }

    public List<Symbol> peek(SymbolType[] types) {
        List<Symbol> lookAhead = scanner.peek(types.length, new ParseSpecification().provider(specification));
        if (lookAhead.size() != types.length) return emptySymbols;
        for (int i = 0; i < lookAhead.size(); i++) {
            if (!lookAhead.get(i).isType(types[i])) return emptySymbols;
        }
        return lookAhead;
    }

    public String parseToAsString(SymbolType terminator) {
        int start = scanner.getOffset();
        scanner.markStart();
        parseTo(terminator);
        return scanner.stringFromStart(start);
    }

    public String parseLiteral(SymbolType terminator) {
        return scanner.makeLiteral(terminator).getContent();
    }

    public Symbol parse(String input) {
        return parseWithParent(input, this);
    }

    public Symbol parseWithParent(String input, Parser parent) {
        return new Parser(parent, currentPage, new Scanner(new TextMaker(variableSource, currentPage.getNamedPage()), input), variableSource, new ParseSpecification().provider(specification)).parse();
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
        return parseTo(terminator, 0);
    }

    public Symbol parseTo(SymbolType terminator, int priority) {
        return parse(new ParseSpecification().terminator(terminator).priority(priority));
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
        return parse(new ParseSpecification().end(end));
    }

    public Symbol parseToEnds(int priority, SymbolProvider provider, SymbolType[] moreEnds) {
        ParseSpecification newSpecification = specification.makeSpecification(provider, moreEnds).priority(priority);
        for (SymbolType end: moreEnds) newSpecification.end(end);
        return parse(newSpecification);
    }

    private Symbol parse(ParseSpecification newSpecification) {
        return new Parser(this, currentPage, scanner, variableSource, newSpecification).parse();
    }

    public Symbol parse() {
        Symbol result = new Symbol(SymbolType.SymbolList);
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(specification);
            if (scanner.isEnd()) break;
            Symbol currentToken = scanner.getCurrent();
            if (specification.endsOn(currentToken.getType()) || parentOwns(currentToken.getType(), specification)) {
                scanner.copy(backup);
                break;
            }
            if (specification.terminatesOn(currentToken.getType())) break;
            Rule currentRule = currentToken.getType().getWikiRule();
            if (currentRule != null) {
                Maybe<Symbol> parsedSymbol = currentRule.parse(currentToken, this);
                if (parsedSymbol.isNothing()) {
                    specification.ignoreFirst(currentToken.getType());
                    scanner.copy(backup);
                }
                else {
                    result.add(parsedSymbol.getValue());
                    specification.clearIgnoresFirst();
                }
            }
            else {
                result.add(currentToken);
                specification.clearIgnoresFirst();
            }
        }
        return result;
    }

    private boolean parentOwns(SymbolType current, ParseSpecification specification) {
        if (parent == null) return false;
        if (parent.specification.hasPriority(specification) && parent.specification.terminatesOn(current)) return true;
        return parent.parentOwns(current, specification);
    }
}
