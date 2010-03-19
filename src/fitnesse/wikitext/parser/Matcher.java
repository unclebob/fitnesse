package fitnesse.wikitext.parser;

public interface Matcher {
    TokenMatch makeMatch(TokenType type, ScanString input);
}
