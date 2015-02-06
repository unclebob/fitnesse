package fitnesse.wikitext.parser;

import java.util.regex.Pattern;

public class TextMaker {
  public static final String eMailPattern = "[\\w-_.]+@[\\w-_.]+\\.[\\w-_.]+";

  private VariableSource variableSource;
  private SourcePage sourcePage;

  public TextMaker(VariableSource variableSource, SourcePage sourcePage) {
    this.variableSource = variableSource;
    this.sourcePage = sourcePage;
  }

  public SymbolMatch make(ParseSpecification specification, String text) {
    if (specification.matchesFor(WikiWord.symbolType)) {
      int length = findWikiWordLength(text);
      if (length > 0) {
        Symbol wikiWord = new Symbol(new WikiWord(sourcePage), text.substring(0, length));
        wikiWord.evaluateVariables(new String[]{WikiWord.REGRACE_LINK}, variableSource);
        return new SymbolMatch(wikiWord, length);
      }
    }
    if (specification.matchesFor(SymbolType.EMail) && isEmailAddress(text)) {
      return new SymbolMatch(SymbolType.EMail, text);
    }
    return new SymbolMatch(SymbolType.Text, text);
  }

  private boolean isEmailAddress(String text) {
    return text.indexOf("@") > 0 && Pattern.matches(eMailPattern, text);
  }

  public int findWikiWordLength(String text) {
    String candidate = text + ".";
    int offset = "<>^.".contains(candidate.substring(0, 1)) ? 1 : 0;
    while (offset < candidate.length()) {
      int dot = candidate.indexOf(".", offset);
      int word = wikiWordLength(candidate.substring(offset, dot));
      if (word == 0) return offset > 1 ? offset - 1 : 0;
      if (offset + word < dot) return offset + word;
      offset = dot + 1;
    }
    return text.length();
  }

  private int wikiWordLength(String candidate) {
    if (candidate.length() < 3) return 0;
    if (!isUpperCaseLetter(candidate, 0)) return 0;
    if (!isDigit(candidate, 1) && !isLowerCaseLetter(candidate, 1)) return 0;

    int lastUpperCaseLetter = 0;
    int i;
    for (i = 2; i < candidate.length(); i++) {
      if (isCharacter(candidate, '_', i)) return 0;
      if (isUpperCaseLetter(candidate, i)) {
        if (i == lastUpperCaseLetter + 1) return 0;
        lastUpperCaseLetter = i;
      } else if (!isDigit(candidate, i) && !isLetter(candidate, i) /*&& !isCharacter(candidate, '.', i)*/) break;
    }
    if (lastUpperCaseLetter > 0 && i > 2) return i;
    return 0;
  }

  private boolean isUpperCaseLetter(String candidate, int offset) {
    return candidate.charAt(offset) >= 'A' && candidate.charAt(offset) <= 'Z';
  }

  private boolean isLowerCaseLetter(String candidate, int offset) {
    return candidate.charAt(offset) >= 'a' && candidate.charAt(offset) <= 'z';
  }

  private boolean isDigit(String candidate, int offset) {
    return candidate.charAt(offset) >= '0' && candidate.charAt(offset) <= '9';
  }

  private boolean isLetter(String candidate, int offset) {
    return isUpperCaseLetter(candidate, offset) || isLowerCaseLetter(candidate, offset);
  }

  private boolean isCharacter(String candidate, char character, int offset) {
    return candidate.charAt(offset) == character;
  }

}
