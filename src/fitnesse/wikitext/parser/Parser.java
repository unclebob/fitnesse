package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class Parser {
    private static final SymbolType[] emptyTypes = new SymbolType[] {};

    public static Parser makeIgnoreFirst(WikiPage currentPage, Scanner scanner, SymbolType type) {
        SymbolType[] types = new SymbolType[] {type};
        return new Parser(currentPage, scanner, new SymbolProvider(), types, types, emptyTypes);
    }

    public static Parser makeEnds(WikiPage currentPage, Scanner scanner, SymbolType[] types) {
        return new Parser(currentPage, scanner, new SymbolProvider(), emptyTypes, emptyTypes, types);
    }

    public static Parser makeIgnoreFirst(WikiPage currentPage, Scanner scanner, SymbolType[] types) {
        return new Parser(currentPage, scanner, new SymbolProvider(), types, types, emptyTypes);
    }

    public static Parser make(WikiPage currentPage, Scanner scanner, SymbolProvider provider, SymbolType type) {
        SymbolType[] types = new SymbolType[] {type};
        return new Parser(currentPage, scanner, provider, types, emptyTypes, emptyTypes);
    }

    public static Parser make(WikiPage currentPage, Scanner scanner, SymbolType type) {
        SymbolType[] types = new SymbolType[] {type};
        return new Parser(currentPage, scanner, new SymbolProvider(), types, emptyTypes, emptyTypes);
    }

    public static Parser make(WikiPage currentPage, String input) {
        return new Parser(currentPage, new Scanner(input), new SymbolProvider(), emptyTypes, emptyTypes, emptyTypes);
    }

    private WikiPage currentPage;
    private SymbolProvider provider;
    private Scanner scanner;
    private SymbolType[] terminators;
    private SymbolType[] ignoresFirst;
    private SymbolType[] ends;

    public Parser(WikiPage currentPage, Scanner scanner, SymbolProvider provider, SymbolType[] terminators, SymbolType[] ignoresFirst, SymbolType[] ends) {
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.provider = provider;
        this.terminators = terminators;
        this.ignoresFirst = ignoresFirst;
        this.ends = ends;
    }

    public Scanner getScanner() { return scanner; }
    public SymbolType[] getTerminators() { return terminators; }
    public SymbolType[] getEnds() { return ends; }

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
            Rule rule = currentToken.getType().getRule();
            if (rule == null) {
                result.add(currentToken);
                ignore.clear();
            }
            else {
                rule.setPage(currentPage);
                Maybe<Symbol> translation = rule.parse(this);
                if (translation.isNothing()) {
                    ignore.add(currentToken.getType());
                    scanner.copy(backup);
                }
                else {
                    result.add(translation.getValue());
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

    public Symbol parseToNewline(WikiPage page) {
        return Parser.makeEnds(page, scanner, makeEndList()).parse();
    }

    public SymbolType[] makeEndList() {
        SymbolType[] parentEnds = getEnds();
        SymbolType[] parentTerminators = getTerminators();
        SymbolType[] ends = new SymbolType[parentTerminators.length + parentEnds.length + 1];
        arraycopy(parentEnds, 0, ends, 0, parentEnds.length);
        arraycopy(parentTerminators, 0, ends, parentEnds.length, parentTerminators.length);
        ends[parentEnds.length + parentTerminators.length] = SymbolType.Newline;
        return ends;
    }
}
