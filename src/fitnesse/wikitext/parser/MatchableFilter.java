package fitnesse.wikitext.parser;

public interface MatchableFilter {
    boolean isValid(Matchable candidate);
}
