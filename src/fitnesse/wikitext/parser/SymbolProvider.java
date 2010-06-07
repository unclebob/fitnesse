package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.HashMap;

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
            SymbolType.Bold,
            SymbolType.Italic, SymbolType.Strike, SymbolType.AnchorReference, SymbolType.WikiWord, SymbolType.Text,
    });
    
    public static final SymbolProvider aliasLinkProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.CloseBracket, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable});

    public static final SymbolProvider linkTargetProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.Literal, SymbolType.Variable});

    public static final SymbolProvider pathRuleProvider = new SymbolProvider(new SymbolType[] {
          SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable});

    public static final SymbolProvider literalTableProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.EndCell, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable});

    private static final char defaultMatch = '\0';

    private HashMap<Character, ArrayList<Matchable>> currentDispatch;
    private ArrayList<SymbolType> symbolTypes;

    public SymbolProvider(SymbolType[] types) {
        symbolTypes = new ArrayList<SymbolType>();
        currentDispatch = new HashMap<Character, ArrayList<Matchable>>();
        currentDispatch.put(defaultMatch, new ArrayList<Matchable>());
        addTypes(types);
    }

    public void addTypes(SymbolType[] types) {
        for (SymbolType symbolType: types) {
            add(symbolType);
        }
    }
    
    public SymbolProvider add(SymbolType symbolType) {
        if (matchesFor(symbolType)) return this;
        symbolTypes.add(symbolType);
        for (char first: symbolType.getWikiMatcher().getFirsts()) {
            if (!currentDispatch.containsKey(first)) currentDispatch.put(first, new ArrayList<Matchable>());
            currentDispatch.get(first).add(new SymbolMatcher(symbolType));
        }
        return this;
    }

    private ArrayList<Matchable> getMatchTypes(Character match) {
        if (currentDispatch.containsKey(match)) return currentDispatch.get(match);
        return currentDispatch.get(defaultMatch);
    }

    public void addMatcher(Matchable matcher) {
        ArrayList<Matchable> defaults = currentDispatch.get(defaultMatch);
        defaults.add(matcher);
    }
    
    public boolean matchesFor(SymbolType type) {
        return symbolTypes.contains(type);
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
            return symbolType.getWikiMatcher().makeMatch(symbolType, input);
        }
    }
}
