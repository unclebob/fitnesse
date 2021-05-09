package fitnesse.slim;

import java.util.function.Function;
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
  protected SymbolMatcher symbolMatcher;
  protected int startingPosition;

  public static class SymbolMatcher {
    private final Function<String, String> symbolStore;
    private final Matcher matcher;
    private String fullString;
    private String symbolName;
    private String symbolValue;

    public SymbolMatcher(Function<String, String> symbolStore, String str, int startingPosition) {
      this.symbolStore = symbolStore;
      matcher = SYMBOL_PATTERN.matcher(str);
      if (matcher.find(startingPosition)) {
        this.fullString = matcher.group();
        resolveSymbol(matcher.group(1));
      }
    }

    public boolean found() {
      return getSymbolName() != null;
    }

    public String getSymbolFound() {
      return fullString;
    }

    public int getSymbolStartPosition() {
      return found() ? matcher.start() : -1;
    }

    public int getSymbolEndPosition() {
      return found() ? matcher.start() + getSymbolName().length() + 1 : -1;
    }

    public String getSymbolName() {
      return symbolName;
    }

    public String getSymbolValue() {
      return symbolValue;
    }

    protected void resolveSymbol(String symbolNameMatch) {
      symbolName = symbolNameMatch;
      symbolValue = symbolStore.apply(symbolName);
      if (symbolValue == null) {
        // no symbol known for entire symbolName try to find a match for the prefix
        for (int i = symbolName.length() - 1; i > 0; i--) {
          String shorterName = symbolName.substring(0, i);
          String shorterValue = symbolStore.apply(shorterName);
          if (shorterValue != null) {
            symbolName = shorterName;
            symbolValue = shorterValue;
            break;
          }
        }
      }
    }
  }

  public static String isSymbolAssignment(String content) {
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
    replaceAllSymbols();
    return replacedString;
  }

  private void replaceAllSymbols() {
    startingPosition = 0;
    while (symbolFound())
      replaceSymbol();
  }

  private void replaceSymbol() {
    String value = formatSymbol();
    String prefix = replacedString.substring(0, symbolMatcher.getSymbolStartPosition());
    String suffix = replacedString.substring(symbolMatcher.getSymbolEndPosition());
    replacedString = prefix + value + suffix;
    int replacementEnd = symbolMatcher.getSymbolStartPosition() + value.length();
    startingPosition = Math.min(replacementEnd, replacedString.length());
  }

  private String formatSymbol() {
    String value = getSymbolValueImpl(symbolMatcher);
    if (value == null) {
      // return the original match without any change if the symbol is not defined
      return symbolMatcher.getSymbolFound();
    } else {
      String symbolName = symbolMatcher.getSymbolName();
      return formatSymbolValue(symbolName, value);
    }
  }

  private boolean symbolFound() {
    symbolMatcher = new SymbolMatcher(this::getSymbolValue, replacedString, startingPosition);
    return symbolMatcher.found();
  }

  protected String formatSymbolValue(String name, String value) {
    return value;
  }

  protected String getSymbolValueImpl(SymbolMatcher symbolMatcher) {
    return symbolMatcher.getSymbolValue();
  }

  // implement this to access the Symbol Store
  protected abstract String getSymbolValue(String symbolName);
}
