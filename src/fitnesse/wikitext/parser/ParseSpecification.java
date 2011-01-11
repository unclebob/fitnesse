package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Arrays;

public class ParseSpecification {
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

    public boolean ignores(Matchable symbolType) {
        return contains(ignoresFirst, symbolType);
    }

    public boolean terminatesOn(SymbolType symbolType) {
        return contains(terminators, symbolType);
    }

    public boolean endsOn(SymbolType symbolType) {
        return contains(ends, symbolType);
    }

    public boolean hasPriority(ParseSpecification other) {
        return priority > other.priority;
    }

    public SymbolMatch findMatch(ScanString input, MatchableFilter filter) {
        return provider.findMatch(input, filter);
    }

    public boolean matchesFor(SymbolType symbolType) {
        return provider.matchesFor(symbolType);
    }

    private boolean contains(Iterable<SymbolType> terminators, Matchable currentType) {
        for (SymbolType terminator: terminators)
            if (currentType.matchesFor(terminator)) return true;
        return false;
    }
}
