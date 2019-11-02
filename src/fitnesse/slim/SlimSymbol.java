package fitnesse.slim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SlimSymbol {
  public static final Pattern SYMBOL_PATTERN = Pattern
      .compile("\\$(([A-Za-z\\p{L}][\\w\\p{L}]*)|`([^`]+)`)");
// This would be a better pattern as it allows to define the end of a symbol name with another $ sign
// but this could break existing tests. See discussion in #790
//  public static final Pattern SYMBOL_PATTERN = Pattern
//      .compile("\\$([A-Za-z\\p{L}][\\w\\p{L}]*)\\$?");
  public static final Pattern SYMBOL_ASSIGNMENT_PATTERN = Pattern
      .compile("\\A\\s*\\$([A-Za-z\\p{L}][\\w\\p{L}]*)\\s*=\\s*\\Z");

  protected String replacedString;
  private Matcher symbolMatcher;
  private int startingPosition;

  public static final String isSymbolAssignment(String content) {
    if (content == null)
      return null;
    Matcher matcher = SYMBOL_ASSIGNMENT_PATTERN.matcher(content);
    return matcher.find() ? matcher.group(1) : null;
  }

  public String replace(String s) {
    if(null == s) return null;

    // Don't replace assignments, return as is
    if (isSymbolAssignment(s) != null)
      return s;

    replacedString = s;
    symbolMatcher = SYMBOL_PATTERN.matcher(s);
    replaceAllSymbols();
    return replacedString;
  }

  private void replaceAllSymbols() {
    startingPosition = 0;
    while (symbolFound())
      replaceSymbol();
  }

  private void replaceSymbol() {
    String value;
    String prefix;
    String suffix;
    String symbolName = symbolMatcher.group(1);
    value = formatSymbol(symbolName);
    prefix = replacedString.substring(0, symbolMatcher.start());
    suffix = replacedString.substring(symbolMatcher.end());
    replacedString = prefix + value + suffix;
    int replacementEnd = symbolMatcher.start() + value.length();
    startingPosition = Math.min(replacementEnd, replacedString.length());
  }

  protected String formatSymbol(String symbolName) {
    String value = getSymbolValue(symbolName);
    if (value == null) {
      // return the original match without any change if the symbol is not defined
      return symbolMatcher.group();
    } else
      return formatSymbolValue(symbolName, value);
  }

  private boolean symbolFound() {
    symbolMatcher = SYMBOL_PATTERN.matcher(replacedString);
    return symbolMatcher.find(startingPosition);
  }

  protected String formatSymbolValue(String name, String value) {
    return value;
  }

  // implement this to access the Symbol Store
  protected abstract String getSymbolValue(String symbolName);
}
