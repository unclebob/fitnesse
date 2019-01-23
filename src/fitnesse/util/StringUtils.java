package fitnesse.util;

public class StringUtils {
  public static boolean isBlank(String str) {

    if (str == null)
      return true;

    int strLen = str.length();
    if (strLen == 0)
      return true;

    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  public static String swapCaseOfFirstLetter(String str) {
    StringBuilder sb = new StringBuilder(str);
    char ch = str.charAt(0);
    if (Character.isUpperCase(ch)) {
      ch = Character.toLowerCase(ch);
    } else if (Character.isTitleCase(ch)) {
      ch = Character.toLowerCase(ch);
    } else if (Character.isLowerCase(ch)) {
      ch = Character.toUpperCase(ch);
    }
    sb.setCharAt(0, ch);
    return sb.toString();
  }
}
