package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolProvider {
    public static final SymbolProvider refactoringProvider = new SymbolProvider( new SymbolType[] {
            SymbolType.Alias, SymbolType.OpenBracket, SymbolType.CloseBracket, SymbolType.Comment, SymbolType.Image,
            SymbolType.Literal, SymbolType.Preformat, SymbolType.Link, SymbolType.Path, SymbolType.WikiWord,
            SymbolType.Newline, SymbolType.Whitespace
    });

    public static final SymbolProvider wikiParsingProvider = new SymbolProvider( new SymbolType[] {
            SymbolType.Link,
            SymbolType.Table, SymbolType.EndCell,
            SymbolType.HashTable, SymbolType.HeaderLine, SymbolType.Literal, SymbolType.Collapsible,
            SymbolType.AnchorName, SymbolType.Contents, SymbolType.CenterLine, SymbolType.Define,
            SymbolType.Include, SymbolType.Meta, SymbolType.NoteLine, SymbolType.Path, SymbolType.PlainTextTable,
            SymbolType.See, SymbolType.Style, SymbolType.Table, SymbolType.LastModified, SymbolType.Image,
            SymbolType.Today,
            SymbolType.HorizontalRule, SymbolType.Table, SymbolType.CloseLiteral, SymbolType.Strike,
            SymbolType.Alias, SymbolType.UnorderedList, SymbolType.OrderedList, SymbolType.Comment, SymbolType.Whitespace, SymbolType.CloseCollapsible,
            SymbolType.Newline, SymbolType.Colon, SymbolType.Comma,
            SymbolType.Evaluator, SymbolType.CloseEvaluator, SymbolType.Variable, SymbolType.Preformat,
            SymbolType.ClosePreformat, SymbolType.OpenParenthesis, SymbolType.OpenBrace, SymbolType.OpenBracket,
            SymbolType.CloseParenthesis, SymbolType.CloseBrace, SymbolType.ClosePlainTextTable, SymbolType.CloseBracket, SymbolType.CloseLiteral,
            SymbolType.Collapsible, SymbolType.HorizontalRule, SymbolType.Bold,
            SymbolType.Italic, SymbolType.Strike, SymbolType.AnchorReference, SymbolType.WikiWord, SymbolType.Text,
    });
    
    private static final char defaultMatch = '\0';

    private HashMap<Character, ArrayList<Matchable>> currentDispatch;

    public SymbolProvider(SymbolType[] types) {
        currentDispatch = new HashMap<Character, ArrayList<Matchable>>();
        currentDispatch.put(defaultMatch, new ArrayList<Matchable>());
        for (SymbolType symbolType: types) {
            if (symbolType.hasAttribute(SymbolType.WikiMatch)) {
                for (char first: ((Matcher)symbolType.getAttribute(SymbolType.WikiMatch)).getFirsts()) {
                    if (!currentDispatch.containsKey(first)) currentDispatch.put(first, new ArrayList<Matchable>());
                    currentDispatch.get(first).add(new SymbolMatcher(symbolType));
                }
            }
            else {
                currentDispatch.get(defaultMatch).add(new SymbolMatcher(symbolType));
            }
        }
    }

    private ArrayList<Matchable> getMatchTypes(Character match) {
        if (currentDispatch.containsKey(match)) return currentDispatch.get(match);
        return currentDispatch.get(defaultMatch);
    }

    public SymbolProvider addTypes(SymbolType[] types) {
        ArrayList<Matchable> defaults = currentDispatch.get(defaultMatch);
        for (SymbolType type: types) {
            if (!matchesFor(defaults, type)) defaults.add(new SymbolMatcher(type));
        }
        return this;
    }

    public void addMatcher(Matchable matcher) {
        ArrayList<Matchable> defaults = currentDispatch.get(defaultMatch);
        defaults.add(matcher);
    }
    
    private boolean matchesFor(List<Matchable> matchables, SymbolType symbolType) {
        for (Matchable matchable: matchables) {
            if (matchable.matchesFor(symbolType)) return true;
        }
        return false;
    }

    public boolean matchesFor(SymbolType type) {
        return matchesFor(currentDispatch.get(defaultMatch), type);
    }

    public SymbolMatch findMatch(ScanString input, MatchableFilter filter) {
        for (Matchable candidate: getMatchTypes(input.charAt(0))) {
            if (filter.isValid(candidate)) {
                SymbolMatch match = candidate.makeMatch(input);
                if (match.isMatch()) return match;
            }
        }
        return SymbolMatch.noMatch;
    }

    private static class SymbolMatcher implements Matchable {
        private SymbolType symbolType;

        public SymbolMatcher(SymbolType symbolType) {
            this.symbolType = symbolType;
        }

        public boolean matchesFor(SymbolType symbolType) {
            return this.symbolType == symbolType;
        }

        public SymbolMatch makeMatch(ScanString input) {
            return symbolType.hasAttribute(SymbolType.WikiMatch)
                    ? ((Matcher)symbolType.getAttribute(SymbolType.WikiMatch)).makeMatch(symbolType, input)
                    : SymbolMatch.noMatch;
        }
    }
}
