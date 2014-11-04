package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Arrays;

public class ParseSpecification {
    public static final int nestingPriority = 2;
    public static final int tablePriority = 1;
    public static final int normalPriority = 0;

    private SymbolProvider provider = SymbolProvider.wikiParsingProvider;
    private ArrayList<SymbolType> terminators = new ArrayList<SymbolType>();
    private ArrayList<SymbolType> ignoresFirst = new ArrayList<SymbolType>();
    private ArrayList<SymbolType> ends = new ArrayList<SymbolType>();
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
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(this);
            if (scanner.isEnd()) break;
            Symbol currentToken = scanner.getCurrent();
            if (endsOn(currentToken.getType()) || parser.parentOwns(currentToken.getType(), this)) {
                scanner.copy(backup);
                break;
            }
            if (terminatesOn(currentToken.getType())) break;
            Rule currentRule = currentToken.getType().getWikiRule();
            Maybe<Symbol> parsedSymbol = currentRule.parse(currentToken, parser);
            if (parsedSymbol.isNothing()) {
                ignoreFirst(currentToken.getType());
                scanner.copy(backup);
            }
            else {
                result.add(parsedSymbol.getValue());
                clearIgnoresFirst();
            }
        }
        return result;
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
