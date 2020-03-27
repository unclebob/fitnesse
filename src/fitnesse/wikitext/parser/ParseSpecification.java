package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Arrays;

public class ParseSpecification {
    public static final int nestingPriority = 2;
    public static final int tablePriority = 1;
    public static final int normalPriority = 0;

    private SymbolProvider provider = SymbolProvider.wikiParsingProvider;
    private ArrayList<SymbolType> terminators = new ArrayList<>();
    private ArrayList<SymbolType> ignoresFirst = new ArrayList<>();
    private ArrayList<SymbolType> ends = new ArrayList<>();
    private int priority = 0;

    public ParseSpecification provider(SymbolProvider provider) {
        this.provider = provider;
        return this;
    }

    public ParseSpecification provider(ParseSpecification specification) {
        this.provider = specification.provider;
        return this;
    }

    public ParseSpecification priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ParseSpecification terminator(SymbolType terminator) {
        terminators.add(terminator);
        return this;
    }

    public ParseSpecification ignoreFirst(SymbolType ignoreFirst) {
        ignoresFirst.add(ignoreFirst);
        return this;
    }

    public void clearIgnoresFirst() {
        ignoresFirst.clear();
    }

    public ParseSpecification end(SymbolType end) {
        ends.add(end);
        return this;
    }

    public ParseSpecification makeSpecification(SymbolProvider providerModel, SymbolType[] providerTypes) {
        SymbolProvider newProvider = new SymbolProvider(providerModel);
        newProvider.addTypes(ends);
        newProvider.addTypes(terminators);
        newProvider.addTypes(Arrays.asList(providerTypes));
        return new ParseSpecification().provider(newProvider);
    }

    public boolean endsOn(SymbolType symbolType) {
        return contains(ends, symbolType);
    }

    public SymbolMatch findMatch(final ScanString input, final int startPosition, final SymbolStream symbols) {
        return provider.findMatch(input.charAt(0), new SymbolMatcher() {
            @Override
            public SymbolMatch makeMatch(Matchable candidate) {
                if (input.getOffset() != startPosition || !ignores(candidate)) {
                    SymbolMatch match = candidate.makeMatch(input, symbols);
                    if (match.isMatch()) return match;
                }
                return SymbolMatch.noMatch;
            }
        });
    }

    public boolean matchesFor(SymbolType symbolType) {
        return provider.matchesFor(symbolType);
    }

    public boolean owns(SymbolType current, ParseSpecification other) {
        return terminatesOn(current) && priority > other.priority;
    }

    public Symbol parse(Parser parser, Scanner scanner) {
        Symbol result = new Symbol(SymbolType.SymbolList);
        result.setStartOffset(scanner.getOffset());
        while (true) {
            Maybe<Symbol> parsedSymbol = parseSymbol(parser, scanner);
            if (parsedSymbol.isNothing()) {
                break;
            } else {
                result.add(parsedSymbol.getValue());
            }
        }
        result.setEndOffset(scanner.getOffset());
        return result;
    }

    /**
     *
     * @param parser parser
     * @param scanner scanner
     * @return a possible value if parser should stop.
     */
    public Maybe<Symbol> parseSymbol(Parser parser, Scanner scanner) {
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(this);
            if (scanner.isEnd()) return Maybe.nothingBecause("scanner is at end of buffer");
            Symbol currentToken = scanner.getCurrent();
            int startOffset = currentToken.getStartOffset();
            if (endsOn(currentToken.getType()) || parser.parentOwns(currentToken.getType(), this)) {
                scanner.copy(backup);
                return Maybe.nothingBecause("At termination symbol or parent owns symbol");
            }
            if (terminatesOn(currentToken.getType())) return Maybe.nothingBecause("At termination symbol");
            Rule currentRule = currentToken.getType().getWikiRule();
            Maybe<Symbol> parsedSymbol = currentRule.parse(currentToken, parser);
            if (parsedSymbol.isNothing()) {
                ignoreFirst(currentToken.getType());
                scanner.copy(backup);
            } else {
                Symbol parsedSymbolValue = parsedSymbol.getValue();
                parsedSymbolValue.setStartOffset(startOffset).setEndOffset(scanner.getOffset());
                clearIgnoresFirst();
                return parsedSymbol;
            }
        }
    }

    private boolean terminatesOn(SymbolType symbolType) {
        return contains(terminators, symbolType);
    }

    private boolean contains(Iterable<SymbolType> terminators, Matchable currentType) {
        for (SymbolType terminator: terminators)
            if (currentType.matchesFor(terminator)) return true;
        return false;
    }

    private boolean ignores(Matchable symbolType) {
        return contains(ignoresFirst, symbolType);
    }
}
