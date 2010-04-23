package fitnesse.wikitext.parser;

public interface Matchable {
    TokenMatch makeMatch(ScanString input);
}
