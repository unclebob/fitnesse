package fitnesse.wikitext.parser;

import java.util.*;

public class SymbolProvider {
    public static final SymbolProvider refactoringProvider = new SymbolProvider( new SymbolType[] {
            Alias.symbolType, SymbolType.OpenBracket, SymbolType.CloseBracket, Comment.symbolType, Image.symbolType,
            Literal.symbolType, Preformat.symbolType, Link.symbolType, Path.symbolType, WikiWord.symbolType,
            SymbolType.Newline, SymbolType.Whitespace
    });

    public static final SymbolProvider wikiParsingProvider = new SymbolProvider( new SymbolType[] {
            Link.symbolType, new Table(), SymbolType.EndCell,
            new HashTable(),  new HeaderLine(), Literal.symbolType, new Collapsible(),
            new AnchorName(), new Contents(), SymbolType.CenterLine, new Define(), new Help(),
            new Include(), SymbolType.Meta, SymbolType.NoteLine, Path.symbolType, new PlainTextTable(),
            See.symbolType, SymbolType.Style, new LastModified(), Image.symbolType,
            new Today(), SymbolType.Delta, 
            new HorizontalRule(), SymbolType.CloseLiteral, SymbolType.Strike,
            Alias.symbolType, SymbolType.UnorderedList, SymbolType.OrderedList, Comment.symbolType, SymbolType.Whitespace, SymbolType.CloseCollapsible,
            SymbolType.Newline, SymbolType.Colon, SymbolType.Comma,
            Evaluator.symbolType, SymbolType.CloseEvaluator, Variable.symbolType, Preformat.symbolType,
            SymbolType.ClosePreformat, SymbolType.OpenParenthesis, SymbolType.OpenBrace, SymbolType.OpenBracket,
            SymbolType.CloseParenthesis, SymbolType.CloseBrace, SymbolType.ClosePlainTextTable, SymbolType.CloseBracket, SymbolType.CloseLiteral,
            SymbolType.Bold,
            SymbolType.Italic, SymbolType.Strike, new AnchorReference(), WikiWord.symbolType, SymbolType.EMail, SymbolType.Text,
    });
    
    public static final SymbolProvider aliasLinkProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.CloseBracket, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});

    public static final SymbolProvider linkTargetProvider = new SymbolProvider(
            new SymbolType[] {Literal.symbolType, Variable.symbolType});

    public static final SymbolProvider pathRuleProvider = new SymbolProvider(new SymbolType[] {
          Evaluator.symbolType, Literal.symbolType, Variable.symbolType});

    public static final SymbolProvider literalTableProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.EndCell, SymbolType.Newline, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});

    private static final char defaultMatch = '\0';

    private HashMap<Character, ArrayList<Matchable>> currentDispatch;
    private ArrayList<SymbolType> symbolTypes;

    public SymbolProvider(Iterable<SymbolType> types) {
        symbolTypes = new ArrayList<SymbolType>();
        currentDispatch = new HashMap<Character, ArrayList<Matchable>>();
        currentDispatch.put(defaultMatch, new ArrayList<Matchable>());
        for (char c = 'a'; c <= 'z'; c++) currentDispatch.put(c, new ArrayList<Matchable>());
        for (char c = 'A'; c <= 'Z'; c++) currentDispatch.put(c, new ArrayList<Matchable>());
        for (char c = '0'; c <= '9'; c++) currentDispatch.put(c, new ArrayList<Matchable>());
        addTypes(types);
    }

    public SymbolProvider(SymbolProvider other)  {
        this(other.symbolTypes);
    }

    public SymbolProvider(SymbolType[] types)  {
        this(Arrays.asList(types));
    }

    public void addTypes(Iterable<SymbolType> types) {
        for (SymbolType symbolType: types) {
            add(symbolType);
        }
    }
    
    public SymbolProvider add(SymbolType symbolType) {
        if (matchesFor(symbolType)) return this;
        symbolTypes.add(symbolType);
        for (Matcher matcher: symbolType.getWikiMatchers()) {
            for (char first: matcher.getFirsts()) {
                if (!currentDispatch.containsKey(first)) currentDispatch.put(first, new ArrayList<Matchable>());
                currentDispatch.get(first).add(symbolType);
            }
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
}
