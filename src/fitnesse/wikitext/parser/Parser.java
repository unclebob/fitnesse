package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class Parser {
    private static final SymbolType[] emptyTypes = new SymbolType[] {};

    public static Parser make(ParsingPage currentPage, String input) {
        return make(currentPage, input, new VariableFinder(currentPage));
    }

    public static Parser make(ParsingPage currentPage, String input, VariableSource variableSource) {
        return new Parser(currentPage, new Scanner(new TextMaker(variableSource), input), new SymbolProvider(), variableSource, emptyTypes, emptyTypes, emptyTypes);
    }

    private ParsingPage currentPage;
    private SymbolProvider provider;
    private VariableSource variableSource;
    private Scanner scanner;
    private SymbolType[] terminators;
    private SymbolType[] ignoresFirst;
    private SymbolType[] ends;
    private Symbol result;

    public Parser(ParsingPage currentPage, Scanner scanner, SymbolProvider provider, SymbolType[] terminators, SymbolType[] ignoresFirst, SymbolType[] ends) {
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.provider = provider;
        this.terminators = terminators;
        this.ignoresFirst = ignoresFirst;
        this.ends = ends;
        this.variableSource = new VariableFinder(currentPage);
    }

    public Parser(ParsingPage currentPage, Scanner scanner, SymbolProvider provider, VariableSource variableSource, SymbolType[] terminators, SymbolType[] ignoresFirst, SymbolType[] ends) {
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.provider = provider;
        this.terminators = terminators;
        this.ignoresFirst = ignoresFirst;
        this.ends = ends;
        this.variableSource = variableSource;
    }

    public Scanner getScanner() { return scanner; }
    public ParsingPage getPage() { return currentPage; }
    public VariableSource getVariableSource() { return variableSource; }
    public SymbolType[] getTerminators() { return terminators; }
    public SymbolType[] getEnds() { return ends; }

    public Symbol getCurrent() { return scanner.getCurrent(); }

    public Maybe<Symbol> getPrevious(SymbolType requestedType) {
        if (result == null) return Symbol.Nothing;
        Maybe<Symbol> lastChild = result.getLastChild();
        if (lastChild.isNothing() || lastChild.getValue().getType() != requestedType) return Symbol.Nothing;
        return lastChild;
    }

    public Symbol parse(String input) {
        return new Parser(currentPage, new Scanner(new TextMaker(variableSource), input), provider, variableSource, emptyTypes, emptyTypes, emptyTypes).parse();
    }

    public Symbol parseToIgnoreFirst(SymbolType type) {
        return parseToIgnoreFirst(new SymbolType[] {type});
    }

    public Symbol parseToIgnoreFirst(SymbolType[] types) {
        return new Parser(currentPage, scanner, provider, variableSource, types, types, emptyTypes).parse();
    }

    public Symbol parseIgnoreFirstWithSymbols(SymbolType ignore, SymbolType[] validSymbols) {
        SymbolType[] ignores = new SymbolType[] {ignore};
        SymbolProvider provider = new SymbolProvider().setTypes(validSymbols);
        return new Parser(currentPage, scanner, provider, variableSource, ignores, ignores, emptyTypes).parse();
    }
    
    public Symbol parseTo(SymbolType terminator) {
        return parseTo(new SymbolType[] {terminator});
    }

    public Symbol parseTo(SymbolType[] terminators) {
        return new Parser(currentPage, scanner, new SymbolProvider(), terminators, emptyTypes, emptyTypes).parse();
    }

    public Symbol parseToWithSymbols(SymbolType terminator, SymbolType[] validSymbols) {
        SymbolType[] terminators = new SymbolType[] {terminator};
        return parseToWithSymbols(terminators, validSymbols);
    }

    public Symbol parseToWithSymbols(SymbolType[] terminators, Matchable[] validSymbols) {
        SymbolProvider provider = new SymbolProvider().setTypes(validSymbols);
        return new Parser(currentPage, scanner, provider, terminators, emptyTypes, emptyTypes).parse();
    }

    public Symbol parseWithEnds(SymbolType[] ends) {
        return new Parser(currentPage, scanner, new SymbolProvider(), emptyTypes, emptyTypes, makeEndList(ends)).parse();
    }

    public Symbol parseWithEnds(SymbolProvider provider, SymbolType[] types) {
        provider.addTypes(types);
        return new Parser(currentPage, scanner, provider, variableSource, emptyTypes, emptyTypes, types).parse();
    }

    public Symbol parse() {
        result = new Symbol(SymbolType.SymbolList);
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
            Rule rule = currentToken.getType().getRule();
            if (rule == null) {
                result.add(currentToken);
                ignore.clear();
            }
            else {
                Maybe<Symbol> translation = rule.parse(this);
                if (translation.isNothing()) {
                    ignore.add(currentToken.getType());
                    scanner.copy(backup);
                }
                else {
                    if (!result.hasLastChild(translation.getValue())) {
                        result.add(translation.getValue());
                    }
                    ignore.clear();
                }
            }
        }
        return result;
    }

    private boolean contains(SymbolType[] terminators, SymbolType currentType) {
        for (SymbolType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }

    public SymbolType[] makeEndList(SymbolType[] terminators) {
        SymbolType[] parentEnds = getEnds();
        SymbolType[] parentTerminators = getTerminators();
        SymbolType[] ends = new SymbolType[parentTerminators.length + parentEnds.length + terminators.length];
        arraycopy(parentEnds, 0, ends, 0, parentEnds.length);
        arraycopy(parentTerminators, 0, ends, parentEnds.length, parentTerminators.length);
        arraycopy(terminators, 0, ends, parentTerminators.length + parentEnds.length, terminators.length);
        return ends;
    }
}
