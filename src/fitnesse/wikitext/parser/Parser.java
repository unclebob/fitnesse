package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Parser {
    private static final SymbolType[] emptyTypes = new SymbolType[] {};
    private static final Collection<SymbolType> emptyTypesList = new ArrayList<SymbolType>();
    private static final ArrayList<Symbol> emptySymbols = new ArrayList<Symbol>();
    
    public static Parser make(ParsingPage currentPage, String input) {
        return make(currentPage, input, SymbolProvider.wikiParsingProvider);
    }

    public static Parser make(ParsingPage currentPage, String input, SymbolProvider provider) {
        return make(currentPage, input, new VariableFinder(currentPage), provider);
    }

    public static Parser make(ParsingPage currentPage, String input, VariableSource variableSource, SymbolProvider provider) {
        return new Parser(currentPage, new Scanner(new TextMaker(variableSource), input), provider, variableSource, emptyTypes, emptyTypes, emptyTypesList);
    }

    private ParsingPage currentPage;
    private SymbolProvider provider;
    private VariableSource variableSource;
    private Scanner scanner;
    private SymbolType[] terminators;
    private SymbolType[] ignoresFirst;
    private Collection<SymbolType> ends;

    public Parser(ParsingPage currentPage, Scanner scanner, SymbolProvider provider, VariableSource variableSource, SymbolType[] terminators, SymbolType[] ignoresFirst, Collection<SymbolType> ends) {
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.provider = provider;
        this.terminators = terminators;
        this.ignoresFirst = ignoresFirst;
        this.ends = ends;
        this.variableSource = variableSource;
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
        List<Symbol> lookAhead = scanner.peek(types.length, provider, new ArrayList<SymbolType>());
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
        return scanner.substring(start, scanner.getOffset() - 1);
    }

    public String parseLiteral(SymbolType terminator) {
        return scanner.makeLiteral(terminator).getContent();
    }

    public Symbol parse(String input) {
        return new Parser(currentPage, new Scanner(new TextMaker(variableSource), input), provider, variableSource, emptyTypes, emptyTypes, emptyTypesList).parse();
    }

    public Symbol parseToIgnoreFirst(SymbolType type) {
        return parseToIgnoreFirst(new SymbolType[] {type});
    }

    public Symbol parseToIgnoreFirst(SymbolType[] types) {
        return new Parser(currentPage, scanner, provider, variableSource, types, types, emptyTypesList).parse();
    }

    public Symbol parseToIgnoreFirstWithSymbols(SymbolType ignore, SymbolProvider provider) {
        SymbolType[] ignores = new SymbolType[] {ignore};
        return new Parser(currentPage, scanner, provider, variableSource, ignores, ignores, emptyTypesList).parse();
    }
    
    public Symbol parseTo(SymbolType terminator) {
        return parseTo(new SymbolType[] {terminator});
    }

    public Symbol parseTo(SymbolType[] terminators) {
        return new Parser(currentPage, scanner, SymbolProvider.wikiParsingProvider, variableSource, terminators, emptyTypes, emptyTypesList).parse();
    }

    public Symbol parseToWithSymbols(SymbolType terminator, SymbolProvider provider) {
        SymbolType[] terminators = new SymbolType[] {terminator};
        return parseToWithSymbols(terminators, provider);
    }

    public Symbol parseToWithSymbols(SymbolType[] terminators, SymbolProvider provider) {
        return new Parser(currentPage, scanner, provider, variableSource, terminators, emptyTypes, emptyTypesList).parse();
    }

    public Symbol parseWithEnds(SymbolType[] ends) {
        return new Parser(currentPage, scanner, SymbolProvider.wikiParsingProvider, variableSource, emptyTypes, emptyTypes, makeEndList(ends)).parse();
    }

    public Symbol parseWithEnds(SymbolProvider provider, SymbolType[] types) {
        Iterable<SymbolType> endList = makeEndList(types);
        SymbolProvider newProvider = new SymbolProvider(provider);
        newProvider.addTypes(endList);
        return new Parser(currentPage, scanner, newProvider, variableSource, emptyTypes, emptyTypes, makeEndList(types)).parse();
    }

    public Symbol parse() {
        Symbol result = new Symbol(SymbolType.SymbolList);
        ArrayList<SymbolType> ignore = new ArrayList<SymbolType>();
        ignore.addAll(Arrays.asList(ignoresFirst));
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(provider, ignore);
            if (scanner.isEnd()) break;
            Symbol currentToken = scanner.getCurrent();
            if (contains(ends, currentToken.getType())) {
                scanner.copy(backup);
                break;
            }
            if (contains(terminators, currentToken.getType())) break;
            Rule currentRule = currentToken.getType().getWikiRule();
            if (currentRule != null) {
                Maybe<Symbol> parsedSymbol = currentRule.parse(currentToken, this);
                if (parsedSymbol.isNothing()) {
                    ignore.add(currentToken.getType());
                    scanner.copy(backup);
                }
                else {
                    result.add(parsedSymbol.getValue());
                    ignore.clear();
                }
            }
            else {
                result.add(currentToken);
                ignore.clear();
            }
        }
        return result;
    }

    private boolean contains(SymbolType[] terminators, SymbolType currentType) {
        for (SymbolType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }

    private boolean contains(Iterable<SymbolType> terminators, SymbolType currentType) {
        for (SymbolType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }

    private Collection<SymbolType> makeEndList(SymbolType[] moreEnds) {
        ArrayList<SymbolType> endList = new ArrayList<SymbolType>();
        endList.addAll(ends);
        endList.addAll(Arrays.asList(terminators));
        endList.addAll(Arrays.asList(moreEnds));
        return endList;
    }
}
