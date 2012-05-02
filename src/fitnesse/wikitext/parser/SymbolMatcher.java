package fitnesse.wikitext.parser;

public interface SymbolMatcher {
    SymbolMatch makeMatch(Matchable candidate);
}
