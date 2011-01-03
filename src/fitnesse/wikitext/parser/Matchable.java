package fitnesse.wikitext.parser;

public interface Matchable {
    boolean matchesFor(SymbolType symbolType);
    SymbolMatch makeMatch(ScanString input);
}
