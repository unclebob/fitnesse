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
}
