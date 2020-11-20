package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolProvider {
  public static final SymbolProvider refactoringProvider = new SymbolProvider(new SymbolType[]{
    Alias.symbolType, SymbolType.OpenBracket, SymbolType.CloseBracket, Comment.symbolType, Image.symbolType,
    Literal.symbolType, Preformat.symbolType, Link.symbolType, Path.symbolType, WikiWord.symbolType,
    SymbolType.Newline, SymbolType.Whitespace
  });

  public static final SymbolProvider wikiParsingProvider = new SymbolProvider(new SymbolType[]{
    Link.symbolType, Table.symbolType,
    new HashTable(), new HeaderLine(), Literal.symbolType, Nesting.symbolType, new Collapsible(),
    new AnchorName(), new Contents(), SymbolType.CenterLine, new Define(), new Help(),
    new Include(), SymbolType.Meta, SymbolType.NoteLine, Path.symbolType, new PlainTextTable(),
    See.symbolType, SymbolType.Style, new LastModified(), Image.symbolType,
    new Today(), SymbolType.Delta,
    new HorizontalRule(), SymbolType.CloseLiteral, SymbolType.Strike,
    Alias.symbolType, SymbolType.UnorderedList, SymbolType.OrderedList, Comment.symbolType, SymbolType.Whitespace, SymbolType.CloseCollapsible,
    SymbolType.Newline, SymbolType.Colon, SymbolType.Comma,
    Evaluator.symbolType, SymbolType.CloseEvaluator, Variable.symbolType, Preformat.symbolType,
    SymbolType.ClosePreformat, SymbolType.OpenParenthesis, SymbolType.OpenBrace, SymbolType.OpenBracket, SymbolType.CloseNesting,
    SymbolType.CloseParenthesis, SymbolType.CloseBrace, SymbolType.ClosePlainTextTable, SymbolType.CloseBracket, SymbolType.CloseLiteral,
    SymbolType.Bold, SymbolType.DateFormatOption,
    SymbolType.Italic, SymbolType.Strike, new AnchorReference(), WikiWord.symbolType, SymbolType.EMail, SymbolType.Text,
    new Headings()
  });

  public static final SymbolProvider noLinksTableParsingProvider = SymbolProvider.copy(wikiParsingProvider)
    .remove(WikiWord.symbolType)
    .remove(SymbolType.EMail)
    .remove(Link.symbolType)
    .add(SymbolType.EndCell);

  public static final SymbolProvider tableParsingProvider = new SymbolProvider(wikiParsingProvider).add(SymbolType.EndCell);

  public static final SymbolProvider aliasLinkProvider = new SymbolProvider(
    new SymbolType[]{SymbolType.CloseBracket, SymbolType.Whitespace, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});

  public static final SymbolProvider linkTargetProvider = new SymbolProvider(
    new SymbolType[]{Literal.symbolType, Variable.symbolType});

  public static final SymbolProvider pathRuleProvider = new SymbolProvider(new SymbolType[]{
    Evaluator.symbolType, Literal.symbolType, Variable.symbolType});

  public static final SymbolProvider literalTableProvider = new SymbolProvider(
    new SymbolType[]{SymbolType.EndCell, SymbolType.Whitespace, SymbolType.Newline, SymbolType.Colon, Evaluator.symbolType, Literal.symbolType, Variable.symbolType});

  // This scheme is used for parsing system properties (accessed by PageData.getVariable()).
  public static final SymbolProvider variableDefinitionSymbolProvider = new SymbolProvider(new SymbolType[]{
    Literal.symbolType, new Define(), new Include(), SymbolType.CloseLiteral, Comment.symbolType, SymbolType.Whitespace,
    SymbolType.Newline, Variable.symbolType, Preformat.symbolType,
    SymbolType.ClosePreformat, SymbolType.Text
  });

  public static SymbolProvider copy(SymbolProvider provider) {
    return new SymbolProvider(provider.symbolTypes);
  }

  static final SymbolProvider preformatProvider = new SymbolProvider(
    new SymbolType[]{SymbolType.ClosePreformat, SymbolType.CloseBrace, SymbolType.CloseLiteral, Literal.symbolType, Variable.symbolType,
      new Today(), SymbolType.Delta, SymbolType.Whitespace, SymbolType.OpenParenthesis, SymbolType.CloseParenthesis, SymbolType.DateFormatOption,
      Evaluator.symbolType, SymbolType.CloseEvaluator});

  private static final char defaultMatch = '\0';

  private Map<Character, ArrayList<Matchable>> currentDispatch;
  private Collection<SymbolType> symbolTypes;
  private SymbolProvider parent = null;

  public SymbolProvider(Iterable<SymbolType> types) {
    symbolTypes = new ArrayList<>();
    currentDispatch = new HashMap<>();
    currentDispatch.put(defaultMatch, new ArrayList<Matchable>());
    for (char c = 'a'; c <= 'z'; c++) currentDispatch.put(c, new ArrayList<Matchable>());
    for (char c = 'A'; c <= 'Z'; c++) currentDispatch.put(c, new ArrayList<Matchable>());
    for (char c = '0'; c <= '9'; c++) currentDispatch.put(c, new ArrayList<Matchable>());
    addTypes(types);
  }

  public SymbolProvider(SymbolProvider parent) {
    this(new SymbolType[]{});
    this.parent = parent;
  }

  public SymbolProvider(SymbolType[] types) {
    this(Arrays.asList(types));
  }

  public void addTypes(Iterable<SymbolType> types) {
    for (SymbolType symbolType : types) {
      add(symbolType);
    }
  }

  public SymbolProvider add(SymbolType symbolType) {
    if (matchesFor(symbolType)) return this;
    symbolTypes.add(symbolType);
    for (Matcher matcher : symbolType.getWikiMatchers()) {
      for (char first : matcher.getFirsts()) {
        if (!currentDispatch.containsKey(first)) currentDispatch.put(first, new ArrayList<Matchable>());
        currentDispatch.get(first).add(symbolType);
      }
    }
    return this;
  }

  public SymbolProvider remove(SymbolType symbolType) {
    if (!matchesFor(symbolType)) return this;
    symbolTypes.remove(symbolType);
    for (Matcher matcher : symbolType.getWikiMatchers()) {
      for (char first : matcher.getFirsts()) {
        if (!currentDispatch.containsKey(first)) currentDispatch.put(first, new ArrayList<Matchable>());
        currentDispatch.get(first).remove(symbolType);
      }
    }
    return this;
  }


  private List<Matchable> getMatchTypes(Character match) {
    if (currentDispatch.containsKey(match)) return currentDispatch.get(match);
    return currentDispatch.get(defaultMatch);
  }

  public boolean matchesFor(SymbolType type) {
    return (parent != null && parent.matchesFor(type)) || symbolTypes.contains(type);
  }

  public SymbolMatch findMatch(Character startCharacter, SymbolMatcher matcher) {
    if (parent != null) {
      SymbolMatch parentMatch = parent.findMatch(startCharacter, matcher);
      if (parentMatch.isMatch()) return parentMatch;
    }
    for (Matchable candidate : getMatchTypes(startCharacter)) {
      SymbolMatch match = matcher.makeMatch(candidate);
      if (match.isMatch()) return match;
    }
    return SymbolMatch.noMatch;
  }
}
