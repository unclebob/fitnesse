package fitnesse.wikitext;

import fitnesse.wikitext.parser.MarkUpSystemV2;
import fitnesse.wikitext.parser.MatchResult;
import fitnesse.wikitext.parser.Matcher;
import fitnesse.wikitext.parser.ScanString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MarkUpSystems {
  public static MarkUpSystems STORE = new MarkUpSystems();

  public MarkUpSystem make(String content) {
    String key = findName(content).toLowerCase();
    return key.length() > 0 && systems.containsKey(key)
      ? systems.get(key).get()
      : new MarkUpSystemV2();
  }

  public void register(String name, Supplier<MarkUpSystem> supplier) {
    systems.put(name.toLowerCase(), supplier);
  }

  public static String findName(String content) {
    ScanString input = new ScanString(content, 0);

    while (true) {
      MatchResult leadingMatch = LEADING_MATCHER.makeMatch(input);
      if (!leadingMatch.isMatched()) break;
      leadingMatch.advance(input);
    }

    MatchResult langMatch = LANG_MATCHER.makeMatch(input);
    if (langMatch.isMatched()) {
      langMatch.advance(input);
      int startName = input.getOffset();
      MatchResult newLineMatch = NEWLINE_MATCHER.findMatch(input);
      if (newLineMatch.isMatched()) {
        return input.rawSubstring(startName, input.getOffset());
      }
    }
    return "";
  }

  private static final Map<String, Supplier<MarkUpSystem>> systems = new HashMap<>();

  private static final Matcher LANG_MATCHER = new Matcher().string("#lang").whitespace();
  private static final Matcher NEWLINE_MATCHER = new Matcher().newLine();
  private static final Matcher LEADING_MATCHER = new Matcher().ignoreWhitespace().newLine();
}
