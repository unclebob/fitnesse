package fitnesse.wikitext.parser;

import java.util.regex.Pattern;

public class WikiWordPath {

    //todo: get rid of these
    private static final String SINGLE_WIKIWORD_REGEXP = "\\b[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+";
    private static final String REGEXP = "(?:[<>^.])?(?:" + SINGLE_WIKIWORD_REGEXP + "[.]?)+\\b";

    public static boolean isSingleWikiWord(String s) {
      return Pattern.matches(SINGLE_WIKIWORD_REGEXP, s);
    }

    public static boolean isWikiWord(String word) {
      return Pattern.matches(REGEXP, word);
    }

    public static String makeWikiWord(String input) {
        if (isWikiWord(input)) return input;
        String base = input;
        while (base.length() < 3) base += "a";
        return base.substring(0, 1).toUpperCase()
                + base.substring(1, base.length() - 1).toLowerCase()
                + base.substring(base.length() - 1).toUpperCase();
    }
    public int findLength(String text) {
        String candidate = text + ".";
        int offset = "<>^.".indexOf(candidate.substring(0, 1)) >= 0 ? 1 : 0;
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
                lastUpperCaseLetter =  i;
            }
            else if (!isDigit(candidate, i) && !isLetter(candidate, i) && !isCharacter(candidate, '.', i)) break;
        }
        if (lastUpperCaseLetter > 0 && i > 2) return i;
        return 0;
    }

    private boolean isUpperCaseLetter(String candidate, int offset) {
        return isLetter(candidate, offset) && Character.isUpperCase(candidate.charAt(offset));
    }

    private boolean isLowerCaseLetter(String candidate, int offset) {
        return isLetter(candidate, offset) && Character.isLowerCase(candidate.charAt(offset));
    }

    private boolean isDigit(String candidate, int offset) {
        return Character.isDigit(candidate.charAt(offset));
    }

    private boolean isLetter(String candidate, int offset) {
        return Character.isLetter(candidate.charAt(offset));
    }

    private boolean isCharacter(String candidate, char character, int offset) {
        return candidate.charAt(offset) == character;
    }
}
