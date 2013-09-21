package fitnesse.wikitext.parser;

public class WikiWordPath {

    public static boolean isSingleWikiWord(String s) {
      return new WikiWordPath().wikiWordLength(s) == s.length();
    }

    public static boolean isWikiWord(String word) {
      return new WikiWordPath().findLength(word) == word.length();
    }

    public static String makeWikiWord(String input) {
        if (isWikiWord(input)) return input;
        StringBuilder base = new StringBuilder(input);
        while (base.length() < 3) base.append("a");
        return base.substring(0, 1).toUpperCase()
                + base.substring(1, base.length() - 1).toLowerCase()
                + base.substring(base.length() - 1).toUpperCase();
    }

    public int findLength(String text) {
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
                lastUpperCaseLetter =  i;
            }
            else if (!isDigit(candidate, i) && !isLetter(candidate, i) /*&& !isCharacter(candidate, '.', i)*/) break;
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
