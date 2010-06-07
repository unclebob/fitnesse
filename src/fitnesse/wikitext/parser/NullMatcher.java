package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.ArrayList;
import java.util.List;

public class NullMatcher extends Matcher {
    @Override
    public List<Character> getFirsts() {
        return new ArrayList<Character>();
    }

    @Override
    public SymbolMatch makeMatch(SymbolType type, ScanString input)  {
        return SymbolMatch.noMatch;
    }
}
